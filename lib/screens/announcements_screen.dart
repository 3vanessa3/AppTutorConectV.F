import 'package:flutter/material.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:intl/intl.dart';

class AnnouncementsScreen extends StatefulWidget {
  const AnnouncementsScreen({Key? key}) : super(key: key);

  @override
  _AnnouncementsScreenState createState() => _AnnouncementsScreenState();
}

class _AnnouncementsScreenState extends State<AnnouncementsScreen> with WidgetsBindingObserver {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final FirebaseAuth _auth = FirebaseAuth.instance;
  bool isTeacher = false;
  bool _isLoading = true;
  String? _error;
  StreamSubscription? _subscription;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    print('AnnouncementsScreen - initState');
    _initializeScreen();
  }

  Future<void> _initializeScreen() async {
    try {
      print('AnnouncementsScreen - Iniciando inicialización');
      await _checkAuth();
      _loadAnnouncements();
    } catch (e) {
      print('AnnouncementsScreen - Error en inicialización: $e');
      setState(() {
        _error = e.toString();
      });
    }
  }

  void _loadAnnouncements() {
    print('AnnouncementsScreen - Iniciando carga de anuncios');
    try {
      // Cancelar suscripción anterior si existe
      _subscription?.cancel();
      
      _subscription = _firestore
          .collection('comunicados')
          .orderBy('fechaCreacion', descending: true)
          .snapshots(includeMetadataChanges: true)
          .listen(
        (snapshot) {
          print('AnnouncementsScreen - Recibidos ${snapshot.docs.length} documentos');
          print('AnnouncementsScreen - From cache: ${snapshot.metadata.isFromCache}');
          print('AnnouncementsScreen - Pending writes: ${snapshot.metadata.hasPendingWrites}');
          
          for (var doc in snapshot.docs) {
            print('AnnouncementsScreen - Documento ${doc.id}:');
            print('  autorNombre: ${doc.data()['autorNombre']}');
            print('  titulo: ${doc.data()['titulo']}');
            print('  tipo: ${doc.data()['tipo']}');
            print('  fechaCreacion: ${doc.data()['fechaCreacion']}');
          }

          if (!mounted) return;
          setState(() {
            _isLoading = false;
          });
        },
        onError: (error) {
          print('AnnouncementsScreen - Error en stream: $error');
          if (!mounted) return;
          setState(() {
            _error = 'Error al cargar comunicados: $error';
            _isLoading = false;
          });
        },
      );
    } catch (e) {
      print('AnnouncementsScreen - Error al configurar stream: $e');
      if (!mounted) return;
      setState(() {
        _error = 'Error al configurar stream: $e';
        _isLoading = false;
      });
    }
  }

  Future<void> _checkAuth() async {
    try {
      // Asegurarse de que hay un usuario autenticado
      var user = _auth.currentUser;
      if (user == null) {
        print('AnnouncementsScreen - No hay usuario, intentando autenticación anónima');
        final credential = await _auth.signInAnonymously();
        user = credential.user;
        print('AnnouncementsScreen - Usuario autenticado anónimamente: ${user?.uid}');
      } else {
        print('AnnouncementsScreen - Usuario ya autenticado: ${user.uid}');
      }

      if (!mounted) return;

      // Verificar si el usuario es profesor
      if (user != null) {
        final profesorDoc = await _firestore.collection('Profesores').doc(user.uid).get();
        setState(() {
          isTeacher = profesorDoc.exists;
          print('AnnouncementsScreen - Es profesor: $isTeacher');
        });
      }
    } catch (e) {
      print('AnnouncementsScreen - Error en _checkAuth: $e');
      if (!mounted) return;
      setState(() {
        _error = 'Error de autenticación: $e';
      });
    }
  }

  @override
  void initState() {
    super.initState();
    _checkUserRole();
  }

  Future<void> _checkUserRole() async {
    final user = _auth.currentUser;
    if (user != null) {
      final doc = await _firestore.collection('Profesores').doc(user.uid).get();
      if (mounted) {
        setState(() {
          isTeacher = doc.exists;
        });
      }
    }
  }

  void _showCreateAnnouncementDialog() {
    final formKey = GlobalKey<FormState>();
    String title = '';
    String description = '';
    String type = 'NEWS';

    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Crear nuevo anuncio'),
        content: Form(
          key: formKey,
          child: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                TextFormField(
                  decoration: const InputDecoration(
                    labelText: 'Título',
                    border: OutlineInputBorder(),
                  ),
                  validator: (value) =>
                      value?.isEmpty ?? true ? 'Este campo es requerido' : null,
                  onSaved: (value) => title = value ?? '',
                ),
                const SizedBox(height: 16),
                TextFormField(
                  decoration: const InputDecoration(
                    labelText: 'Descripción',
                    border: OutlineInputBorder(),
                  ),
                  maxLines: 3,
                  validator: (value) =>
                      value?.isEmpty ?? true ? 'Este campo es requerido' : null,
                  onSaved: (value) => description = value ?? '',
                ),
                const SizedBox(height: 16),
                DropdownButtonFormField<String>(
                  decoration: const InputDecoration(
                    border: OutlineInputBorder(),
                  ),
                  value: type,
                  items: const [
                    DropdownMenuItem(value: 'NEWS', child: Text('Noticia')),
                    DropdownMenuItem(value: 'EVENT', child: Text('Evento')),
                    DropdownMenuItem(value: 'COURSE', child: Text('Curso')),
                  ],
                  onChanged: (value) => type = value ?? 'NEWS',
                ),
              ],
            ),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancelar'),
          ),
          ElevatedButton(
            onPressed: () async {
              if (formKey.currentState?.validate() ?? false) {
                formKey.currentState?.save();
                await _createAnnouncement(title, description, type);
                if (mounted) {
                  Navigator.pop(context);
                }
              }
            },
            child: const Text('Crear'),
          ),
        ],
      ),
    );
  }

  Future<void> _createAnnouncement(
      String title, String description, String type) async {
    try {
      final user = _auth.currentUser;
      if (user == null) return;

      final profesorDoc =
          await _firestore.collection('Profesores').doc(user.uid).get();
      final authorName = profesorDoc.get('nombre') as String? ?? 'Nombre del Tutor';

      final now = DateTime.now();
      final formattedDate = DateFormat('dd/MM/yyyy HH:mm').format(now);

      await _firestore.collection('comunicados').add({
        'autorNombre': authorName,
        'titulo': title,
        'descripcion': description,
        'tipo': type,
        'fechaCreacion': formattedDate,
        'likesCount': 0,
        'commentCount': 0,
      });

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Comunicado creado exitosamente')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Error al crear comunicado: $e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text('Comunicados'),
        backgroundColor: Theme.of(context).primaryColor,
      ),
      body: StreamBuilder<QuerySnapshot>(
        stream: _firestore
            .collection('comunicados')
            .snapshots(includeMetadataChanges: true),
        builder: (context, snapshot) {
          if (snapshot.hasError) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.error_outline, size: 48, color: Colors.red),
                  const SizedBox(height: 16),
                  Text(
                    'Error al cargar comunicados:\n${snapshot.error}',
                    textAlign: TextAlign.center,
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () {
                      setState(() {
                        // Esto forzará una reconstrucción y nuevo intento
                      });
                    },
                    child: const Text('Reintentar'),
                  ),
                ],
              ),
            );
          }

          if (snapshot.connectionState == ConnectionState.waiting && !snapshot.hasData) {
            return const Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  CircularProgressIndicator(),
                  SizedBox(height: 16),
                  Text('Cargando comunicados...'),
                ],
              ),
            );
          }

          final comunicados = snapshot.data?.docs ?? [];
          
          // Debug: Imprimir los datos que llegan de Firestore
          print('Número de comunicados: ${comunicados.length}');
          for (var doc in comunicados) {
            print('Documento ID: ${doc.id}');
            print('Datos: ${doc.data()}');
          }
          
          if (comunicados.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.announcement_outlined, size: 64, color: Colors.grey),
                  const SizedBox(height: 16),
                  const Text(
                    'No hay comunicados disponibles',
                    style: TextStyle(fontSize: 16),
                  ),
                  if (isTeacher) ...[  
                    const SizedBox(height: 16),
                    ElevatedButton.icon(
                      onPressed: _showCreateAnnouncementDialog,
                      icon: const Icon(Icons.add),
                      label: const Text('Crear el primer comunicado'),
                    ),
                  ],
                ],
              ),
            );
          }

          return ListView.builder(
            padding: const EdgeInsets.all(8),
            itemCount: comunicados.length,
            itemBuilder: (context, index) {
              final doc = comunicados[index];
              final data = doc.data() as Map<String, dynamic>;
              print('Renderizando comunicado: $data'); // Debug
              
              // Obtener los campos con valores por defecto si son null
              final titulo = data['titulo'] as String? ?? 'Sin título';
              final descripcion = data['descripcion'] as String? ?? 'Sin descripción';
              final autorNombre = data['autorNombre'] as String? ?? 'Autor desconocido';
              final tipo = data['tipo'] as String? ?? 'NEWS';
              final likesCount = data['likesCount'] as int? ?? 0;
              final commentCount = data['commentCount'] as int? ?? 0;
              
              // Manejo de fecha con valor por defecto
              String formattedDate;
              try {
                final fechaStr = data['fechaCreacion'] as String?;
                if (fechaStr != null) {
                  formattedDate = fechaStr;
                } else {
                  formattedDate = DateFormat('dd/MM/yyyy HH:mm').format(DateTime.now());
                }
              } catch (e) {
                print('Error al parsear fecha: $e');
                formattedDate = DateFormat('dd/MM/yyyy HH:mm').format(DateTime.now());
              }

              return Card(
                elevation: 1,
                margin: const EdgeInsets.symmetric(vertical: 4, horizontal: 8),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15)),
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          CircleAvatar(
                            radius: 20,
                            backgroundColor: Colors.grey[300],
                            child: Icon(Icons.person, color: Colors.grey[600]),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  autorNombre,
                                  style: const TextStyle(
                                    fontWeight: FontWeight.w500,
                                    fontSize: 16,
                                  ),
                                ),
                                Text(
                                  formattedDate,
                                  style: TextStyle(
                                    color: Colors.grey[600],
                                    fontSize: 14,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 12,
                              vertical: 6,
                            ),
                            decoration: BoxDecoration(
                              color: _getTypeColor(tipo),
                              borderRadius: BorderRadius.circular(20),
                            ),
                            child: Text(
                              tipo,
                              style: const TextStyle(
                                color: Colors.white,
                                fontSize: 12,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      Text(
                        titulo,
                        style: const TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        descripcion,
                        style: const TextStyle(fontSize: 16),
                      ),
                      const SizedBox(height: 16),
                      Row(
                        children: [
                          InkWell(
                            onTap: () {
                              // Implementar lógica de likes
                            },
                            child: Row(
                              children: [
                                Icon(Icons.favorite_border,
                                    color: Colors.grey[600], size: 20),
                                const SizedBox(width: 4),
                                Text('$likesCount',
                                    style: TextStyle(color: Colors.grey[600])),
                              ],
                            ),
                          ),
                          const SizedBox(width: 24),
                          InkWell(
                            onTap: () {
                              // Implementar lógica de comentarios
                            },
                            child: Row(
                              children: [
                                Icon(Icons.chat_bubble_outline,
                                    color: Colors.grey[600], size: 20),
                                const SizedBox(width: 4),
                                Text('$commentCount',
                                    style: TextStyle(color: Colors.grey[600])),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              );
            },
          );
        },
      ),
      floatingActionButton: isTeacher
          ? FloatingActionButton(
              onPressed: _showCreateAnnouncementDialog,
              child: const Icon(Icons.add),
              backgroundColor: Theme.of(context).primaryColor,
            )
          : null,
    );
  }

  Color _getTypeColor(String type) {
    switch (type.toUpperCase()) {
      case 'NEWS':
        return Colors.orange;
      case 'EVENT':
        return Colors.purple;
      case 'NOTICE':
        return Colors.orange;
      default:
        return Colors.grey;
    }
  }
}
