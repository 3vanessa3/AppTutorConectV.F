import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';

class FirebaseTutorService {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;
  final FirebaseAuth _auth = FirebaseAuth.instance;

  // Obtener el ID del usuario actual
  String? get currentUserId => _auth.currentUser?.uid;

  // Verificar si el usuario es profesor
  Future<bool> isUserProfesor() async {
    if (currentUserId == null) return false;
    
    final doc = await _firestore
        .collection('Profesores')
        .doc(currentUserId)
        .get();
    
    return doc.exists;
  }

  // Guardar datos del profesor
  Future<void> saveTutorData({
    required String nombre,
    required String edad,
    required String especialidad,
    required String horario,
    required String modalidad,
    required String email,
    required String telefono,
    required String whatsapp,
    required String ubicacion,
  }) async {
    if (currentUserId == null) {
      throw Exception('Usuario no autenticado');
    }

    if (!await isUserProfesor()) {
      throw Exception('El usuario no es profesor');
    }

    final data = {
      'nombre': nombre,
      'edad': edad,
      'especialidad': especialidad,
      'horario': horario,
      'modalidad': modalidad,
      'email': email,
      'telefono': telefono,
      'whatsapp': whatsapp,
      'ubicacion': ubicacion,
      'lastUpdated': FieldValue.serverTimestamp(),
    };

    await _firestore
        .collection('Datos_Profesor')
        .doc(currentUserId)
        .set(data, SetOptions(merge: true));
  }

  // Obtener datos del profesor
  Future<Map<String, dynamic>?> getTutorData() async {
    if (currentUserId == null) return null;

    final doc = await _firestore
        .collection('Datos_Profesor')
        .doc(currentUserId)
        .get();

    return doc.data();
  }
}
