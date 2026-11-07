using System.ComponentModel.DataAnnotations;

namespace CampusLibrary.WebApi.DTOs;

public class BookDto
{
    public int Id { get; set; }
    public bool IsAvailable { get; set; }
    public required string Title { get; set; }
    public required string Isbn { get; set; }
    public string? Author { get; set; }
    public int? YearPublished { get; set; }
}

public class CreateBookDto
{
    [Required, MaxLength(200)]
    public required string Title { get; set; }
    [Required, MaxLength(20)]
    public required string Isbn { get; set; }
    [MaxLength(100)]
    public string? Author { get; set; }
    [Range(1, 9999)]
    public int? YearPublished { get; set; }
}

public class UpdateBookDto
{
    [Required, MaxLength(200)]
    public required string Title { get; set; }
    [Required, MaxLength(20)]
    public required string Isbn { get; set; }
    [MaxLength(100)]
    public string? Author { get; set; }
    [Range(1, 9999)]
    public int? YearPublished { get; set; }
}
