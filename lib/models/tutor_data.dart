class TutorData {
  final String nombre;
  final String edad;
  final String especialidad;
  final String horario;
  final String modalidad;
  final String email;
  final String telefono;
  final String whatsapp;
  final String ubicacion;

  TutorData({
    required this.nombre,
    required this.edad,
    required this.especialidad,
    required this.horario,
    required this.modalidad,
    required this.email,
    required this.telefono,
    required this.whatsapp,
    required this.ubicacion,
  });

  Map<String, dynamic> toMap() {
    return {
      'nombre': nombre,
      'edad': edad,
      'especialidad': especialidad,
      'horario': horario,
      'modalidad': modalidad,
      'email': email,
      'telefono': telefono,
      'whatsapp': whatsapp,
      'ubicacion': ubicacion,
    };
  }

  factory TutorData.fromMap(Map<String, dynamic> map) {
    return TutorData(
      nombre: map['nombre'] ?? '',
      edad: map['edad'] ?? '',
      especialidad: map['especialidad'] ?? '',
      horario: map['horario'] ?? '',
      modalidad: map['modalidad'] ?? '',
      email: map['email'] ?? '',
      telefono: map['telefono'] ?? '',
      whatsapp: map['whatsapp'] ?? '',
      ubicacion: map['ubicacion'] ?? '',
    );
  }
}
