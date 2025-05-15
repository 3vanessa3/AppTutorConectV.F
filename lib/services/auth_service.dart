import 'package:firebase_auth/firebase_auth.dart';
import 'package:cloud_firestore/cloud_firestore.dart';

class AuthService {
  final FirebaseAuth _auth = FirebaseAuth.instance;
  final FirebaseFirestore _firestore = FirebaseFirestore.instance;

  // Register user
  Future<UserCredential?> registerUser({
    required String fullName,
    required String email,
    required String phone,
    required String password,
    required bool isTutor,
  }) async {
    try {
      // Create user with email and password
      UserCredential userCredential = await _auth.createUserWithEmailAndPassword(
        email: email,
        password: password,
      );

      // Save additional user data to Firestore
      await _firestore.collection('users').doc(userCredential.user!.uid).set({
        'fullName': fullName,
        'email': email,
        'phone': phone,
        'isTutor': isTutor,
        'createdAt': FieldValue.serverTimestamp(),
      });

      return userCredential;
    } on FirebaseAuthException catch (e) {
      throw _handleAuthException(e);
    }
  }

  // Password reset
  Future<void> resetPassword(String email) async {
    try {
      // Buscar en la colección de Profesores
      var profesorSnapshot = await _firestore
          .collection('Profesores')
          .where('email', isEqualTo: email)
          .limit(1)
          .get();

      // Si no está en Profesores, buscar en Estudiantes
      if (profesorSnapshot.docs.isEmpty) {
        var estudianteSnapshot = await _firestore
            .collection('Estudiantes')
            .where('email', isEqualTo: email)
            .limit(1)
            .get();

        // Si no está en ninguna colección, lanzar error
        if (estudianteSnapshot.docs.isEmpty) {
          throw FirebaseAuthException(
            code: 'user-not-found',
            message: 'No existe usuario con este correo electrónico.',
          );
        }
      }

      // Si el email existe en alguna colección, enviar el email de recuperación
      await _auth.sendPasswordResetEmail(
        email: email,
        actionCodeSettings: ActionCodeSettings(
          url: 'https://bd-tutorconnect.firebaseapp.com/__/auth/action?mode=action&oobCode=code',
          handleCodeInApp: true,
          androidPackageName: 'com.example.tutorconnect',
          androidInstallApp: true,
          androidMinimumVersion: '12',
        ),
      );
    } on FirebaseAuthException catch (e) {
      throw _handleAuthException(e);
    }
  }

  // Sign in
  Future<UserCredential> signIn(String email, String password) async {
    try {
      return await _auth.signInWithEmailAndPassword(
        email: email,
        password: password,
      );
    } on FirebaseAuthException catch (e) {
      throw _handleAuthException(e);
    }
  }

  // Sign out
  Future<void> signOut() async {
    await _auth.signOut();
  }

  // Helper method to handle Firebase Auth exceptions
  String _handleAuthException(FirebaseAuthException e) {
    switch (e.code) {
      case 'weak-password':
        return 'La contraseña proporcionada es demasiado débil.';
      case 'email-already-in-use':
        return 'Ya existe una cuenta con este correo electrónico.';
      case 'invalid-email':
        return 'El correo electrónico no es válido.';
      case 'user-not-found':
        return 'No existe usuario con este correo electrónico.';
      case 'wrong-password':
        return 'Contraseña incorrecta.';
      default:
        return 'Ocurrió un error: ${e.message}';
    }
  }
}
