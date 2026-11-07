namespace CampusLibrary.Data.Entities;

public class Book
{
    public int Id { get; set; }
    public required string Title { get; set; }
    public required string Isbn { get; set; }
    public string? Author { get; set; }
    public int? YearPublished { get; set; }

    public ICollection<Loan> Loans { get; set; } = new List<Loan>();
}
