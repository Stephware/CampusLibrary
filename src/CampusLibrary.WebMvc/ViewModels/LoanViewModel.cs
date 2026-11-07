namespace CampusLibrary.WebMvc.ViewModels;

public class LoanViewModel
{
    public int Id { get; set; }
    public required string BookTitle { get; set; }
    public required string Isbn { get; set; }
    public required string MemberName { get; set; }
    public required string StudentId { get; set; }
    public DateTime BorrowDate { get; set; }
    public DateTime DueDate { get; set; }
    public bool IsOverdue => DueDate < DateTime.UtcNow;
}
