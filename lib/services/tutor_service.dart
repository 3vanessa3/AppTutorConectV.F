import 'package:cloud_firestore/cloud_firestore.dart';
import '../models/tutor_data.dart';
import '../models/tutor_experiencia.dart';

class TutorService {
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;

  // Guardar o actualizar datos del tutor
  Future<void> saveTutorData(String tutorId, TutorData data) async {
    await _firestore
        .collection('Datos_Profesor')
        .doc(tutorId)
        .set(data.toMap());
  }

  // Obtener datos del tutor
  Future<TutorData?> getTutorData(String tutorId) async {
    final doc = await _firestore
        .collection('Datos_Profesor')
        .doc(tutorId)
        .get();
    
    if (doc.exists) {
      return TutorData.fromMap(doc.data()!);
    }
    return null;
  }

  // Guardar experiencia del tutor
  Future<void> saveExperiencia(String tutorId, List<TutorExperiencia> experiencias) async {
    // Verificar que no haya más de 3 experiencias
    if (experiencias.length > 3) {
      throw Exception('No se pueden guardar más de 3 experiencias');
    }

    final batch = _firestore.batch();
    final experienciasRef = _firestore
        .collection('Experiencia_Profesor')
        .doc(tutorId)
        .collection('experiencias');

    // Primero eliminar todas las experiencias existentes
    final existingDocs = await experienciasRef.get();
    for (var doc in existingDocs.docs) {
      batch.delete(doc.reference);
    }

    // Agregar las nuevas experiencias
    for (var experiencia in experiencias) {
      final docRef = experienciasRef.doc(); // Crear nuevo documento con ID automático
      batch.set(docRef, experiencia.toMap());
    }

    await batch.commit();
  }

  // Obtener experiencias del tutor
  Future<List<TutorExperiencia>> getExperiencias(String tutorId) async {
    final querySnapshot = await _firestore
        .collection('Experiencia_Profesor')
        .doc(tutorId)
        .collection('experiencias')
        .orderBy('orden')
        .get();

    return querySnapshot.docs
        .map((doc) => TutorExperiencia.fromMap(doc.data()))
        .toList();
  }
}
