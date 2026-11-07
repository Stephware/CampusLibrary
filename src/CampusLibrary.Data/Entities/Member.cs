namespace CampusLibrary.Data.Entities;

public class Member
{
    public int Id { get; set; }
    public required string Name { get; set; }
    public required string Email { get; set; }
    public required string StudentId { get; set; }
    public DateTime RegisteredDate { get; set; }

    public ICollection<Loan> Loans { get; set; } = new List<Loan>();
}
