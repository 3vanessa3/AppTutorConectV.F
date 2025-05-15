class TutorExperiencia {
  final String titulo;
  final String descripcion;
  final int orden;

  TutorExperiencia({
    required this.titulo,
    required this.descripcion,
    required this.orden,
  });

  Map<String, dynamic> toMap() {
    return {
      'titulo': titulo,
      'descripcion': descripcion,
      'orden': orden,
    };
  }

  factory TutorExperiencia.fromMap(Map<String, dynamic> map) {
    return TutorExperiencia(
      titulo: map['titulo'] ?? '',
      descripcion: map['descripcion'] ?? '',
      orden: map['orden'] ?? 0,
    );
  }
}
