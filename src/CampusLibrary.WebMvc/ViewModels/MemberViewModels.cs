using System.ComponentModel.DataAnnotations;

namespace CampusLibrary.WebMvc.ViewModels;

public class MemberFormViewModel
{
    [Required, StringLength(100), Display(Name = "Full name")]
    public string Name { get; set; } = "";

    [Required, EmailAddress, StringLength(100)]
    public string Email { get; set; } = "";

    [Required, StringLength(20), Display(Name = "Student ID")]
    public string StudentId { get; set; } = "";
}

public class MemberViewModel : MemberFormViewModel
{
    public int Id { get; set; }
    public DateTime RegisteredDate { get; set; }
}
