import 'package:flutter/material.dart';
import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import '../models/tutor_data.dart';

class TutorDatosScreen extends StatefulWidget {
  const TutorDatosScreen({Key? key}) : super(key: key);

  @override
  _TutorDatosScreenState createState() => _TutorDatosScreenState();
}

class _TutorDatosScreenState extends State<TutorDatosScreen> {
  bool isEditing = false;
  final _formKey = GlobalKey<FormState>();
  
  // Controladores para los campos de texto
  final _nombreController = TextEditingController();
  final _edadController = TextEditingController();
  final _especialidadController = TextEditingController();
  final _horarioController = TextEditingController();
  final _modalidadController = TextEditingController();
  final _emailController = TextEditingController();
  final _telefonoController = TextEditingController();
  final _whatsappController = TextEditingController();
  final _ubicacionController = TextEditingController();

  Future<void> _testFirebaseConnection() async {
    try {
      final user = FirebaseAuth.instance.currentUser;
      print('Usuario actual: ${user?.uid}');
      
      if (user != null) {
        // Intenta una escritura de prueba
        await FirebaseFirestore.instance
            .collection('Datos_Profesor')
            .doc(user.uid)
            .set({'test': 'test'}, SetOptions(merge: true));
        print('Prueba de escritura exitosa');
        
        // Intenta una lectura
        final doc = await FirebaseFirestore.instance
            .collection('Datos_Profesor')
            .doc(user.uid)
            .get();
        print('Documento existe: ${doc.exists}');
        print('Datos actuales: ${doc.data()}');
      } else {
        print('No hay usuario autenticado');
      }
    } catch (e) {
      print('Error en prueba de Firebase: $e');
    }
  }

  @override
  void initState() {
    super.initState();
    _testFirebaseConnection().then((_) => _loadTutorData());
  }

  Future<void> _loadTutorData() async {
    try {
      final user = FirebaseAuth.instance.currentUser;
      if (user == null) return;

      final doc = await FirebaseFirestore.instance
          .collection('datos_profesor')
          .doc(user.uid)
          .get();

      if (doc.exists) {
        final data = doc.data() as Map<String, dynamic>;
        setState(() {
          _nombreController.text = data['Nombre'] ?? '';
          _edadController.text = data['Edad'] ?? '';
          _especialidadController.text = data['Especialidad'] ?? '';
          _horarioController.text = data['Horario'] ?? '';
          _modalidadController.text = data['Modalidades'] ?? '';
          _emailController.text = data['Email'] ?? '';
          _telefonoController.text = data['Telefono'] ?? '';
          _whatsappController.text = data['Whatsapp'] ?? '';
          _ubicacionController.text = data['Ubicacion'] ?? '';
        });
      }
    } catch (e) {
      print('Error al cargar datos: $e');
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error al cargar datos: $e'),
          backgroundColor: Colors.red,
        ),
      );
    }
    try {
      final userId = FirebaseAuth.instance.currentUser?.uid;
      if (userId == null) return;

      final doc = await FirebaseFirestore.instance
          .collection('Datos_Profesor')
          .doc(userId)
          .get();

      if (doc.exists) {
        final data = doc.data() as Map<String, dynamic>;
        setState(() {
          _nombreController.text = data['nombre'] ?? '';
          _edadController.text = data['edad'] ?? '';
          _especialidadController.text = data['especialidad'] ?? '';
          _horarioController.text = data['horario'] ?? '';
          _modalidadController.text = data['modalidad'] ?? '';
          _emailController.text = data['email'] ?? '';
          _telefonoController.text = data['telefono'] ?? '';
          _whatsappController.text = data['whatsapp'] ?? '';
          _ubicacionController.text = data['ubicacion'] ?? '';
        });
      }
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error al cargar datos: \$e')),
      );
    }
  }

  Future<void> _saveChanges() async {
    try {
      final user = FirebaseAuth.instance.currentUser;
      if (user == null) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('No hay usuario autenticado')),
        );
        return;
      }

      // Crear el documento con la estructura exacta de Firebase
      await FirebaseFirestore.instance
          .collection('datos_profesor')
          .doc(user.uid)
          .set({
        'Edad': _edadController.text.trim(),
        'Email': _emailController.text.trim(),
        'Especialidad': _especialidadController.text.trim(),
        'Horario': _horarioController.text.trim(),
        'Modalidades': _modalidadController.text.trim(),
        'Nombre': _nombreController.text.trim(),
        'Telefono': _telefonoController.text.trim(),
        'Ubicacion': _ubicacionController.text.trim(),
        'Whatsapp': _whatsappController.text.trim(),
      });

      setState(() {
        isEditing = false;
      });

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Datos actualizados correctamente'),
          backgroundColor: Colors.green,
        ),
      );

      // Recargar los datos para mostrar los cambios
      _loadTutorData();
    } catch (e) {
      print('Error al guardar datos: $e');
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Error al guardar: $e'),
          backgroundColor: Colors.red,
        ),
      );
    }
    // Verificar autenticación
    final user = FirebaseAuth.instance.currentUser;
    if (user == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('No hay usuario autenticado')),
      );
      return;
    }

    // Verificar que el usuario es profesor
    try {
      final profesorDoc = await FirebaseFirestore.instance
          .collection('Profesores')
          .doc(user.uid)
      }

      await FirebaseFirestore.instance
          .collection('Datos_Profesor')
          .doc(FirebaseAuth.instance.currentUser?.uid)
          .set({
        'nombre': _nombreController.text.trim(),
        'edad': _edadController.text.trim(),
        'especialidad': _especialidadController.text.trim(),
        'horario': _horarioController.text.trim(),
        'modalidad': _modalidadController.text.trim(),
        'email': _emailController.text.trim(),
        'telefono': _telefonoController.text.trim(),
        'whatsapp': _whatsappController.text.trim(),
        'ubicacion': _ubicacionController.text.trim(),
        'lastUpdated': FieldValue.serverTimestamp(),
      });

      setState(() {
        isEditing = false;
      });

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Datos guardados correctamente')),
      );
    } catch (e) {
      print('Error al guardar: $e');
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Error al guardar: $e')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Datos del Tutor'),
        backgroundColor: const Color(0xFF6750A4), // Color morado
        actions: [
          if (!isEditing)
            IconButton(
              icon: const Icon(Icons.edit),
              onPressed: () => setState(() => isEditing = true),
            ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildTextField(
                controller: _nombreController,
                label: 'Nombre',
                enabled: isEditing,
              ),
              _buildTextField(
                controller: _edadController,
                label: 'Edad',
                enabled: isEditing,
              ),
              _buildTextField(
                controller: _especialidadController,
                label: 'Especialidad',
                enabled: isEditing,
              ),
              _buildTextField(
                controller: _horarioController,
                label: 'Horario',
                enabled: isEditing,
              ),
              _buildTextField(
                controller: _modalidadController,
                label: 'Modalidades',
                enabled: isEditing,
              ),
              _buildTextField(
                controller: _emailController,
                label: 'Email',
                enabled: isEditing,
                keyboardType: TextInputType.emailAddress,
              ),
              _buildTextField(
                controller: _telefonoController,
                label: 'Teléfono',
                enabled: isEditing,
                keyboardType: TextInputType.phone,
              ),
              _buildTextField(
                controller: _whatsappController,
                label: 'WhatsApp',
                enabled: isEditing,
                keyboardType: TextInputType.phone,
              ),
              _buildTextField(
                controller: _ubicacionController,
                label: 'Ubicación',
                enabled: isEditing,
              ),
              if (isEditing)
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 16.0),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: [
                      ElevatedButton(
                        onPressed: () => setState(() => isEditing = false),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.grey,
                        ),
                        child: const Text('Cancelar'),
                      ),
                      ElevatedButton(
                        onPressed: _saveChanges,
                        style: ElevatedButton.styleFrom(
                          backgroundColor: const Color(0xFF6750A4),
                        ),
                        child: const Text('Guardar'),
                      ),
                    ],
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTextField({
    required TextEditingController controller,
    required String label,
    required bool enabled,
    TextInputType? keyboardType,
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: TextFormField(
        controller: controller,
        enabled: enabled,
        keyboardType: keyboardType,
        decoration: InputDecoration(
          labelText: label,
          border: const OutlineInputBorder(),
          filled: !enabled,
          fillColor: enabled ? null : Colors.grey[200],
        ),
        validator: (value) {
          if (value == null || value.isEmpty) {
            return 'Este campo es requerido';
          }
          return null;
        },
      ),
    );
  }

  @override
  void dispose() {
    _nombreController.dispose();
    _edadController.dispose();
    _especialidadController.dispose();
    _horarioController.dispose();
    _modalidadController.dispose();
    _emailController.dispose();
    _telefonoController.dispose();
    _whatsappController.dispose();
    _ubicacionController.dispose();
    super.dispose();
  }
}
