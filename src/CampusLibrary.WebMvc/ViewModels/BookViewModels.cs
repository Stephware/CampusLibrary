using System.ComponentModel.DataAnnotations;

namespace CampusLibrary.WebMvc.ViewModels;

public class BookFormViewModel
{
    [Required, StringLength(200)]
    public string Title { get; set; } = "";

    [Required, StringLength(20), Display(Name = "ISBN")]
    public string Isbn { get; set; } = "";

    [StringLength(100)]
    public string? Author { get; set; }

    [Range(1, 9999), Display(Name = "Publication year")]
    public int? YearPublished { get; set; }
}

public class BookViewModel : BookFormViewModel
{
    public int Id { get; set; }
}
