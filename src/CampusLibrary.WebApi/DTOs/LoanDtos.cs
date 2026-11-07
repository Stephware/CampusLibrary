using System.ComponentModel.DataAnnotations;

namespace CampusLibrary.WebApi.DTOs;

public class LoanDto
{
    public int Id { get; set; }
    public int BookId { get; set; }
    public string? BookTitle { get; set; }
    public string? BookAuthor { get; set; }
    public int MemberId { get; set; }
    public DateTime BorrowDate { get; set; }
    public DateTime DueDate { get; set; }
    public DateTime? ReturnDate { get; set; }
}

public class CreateLoanDto
{
    [Range(1, int.MaxValue)]
    public int BookId { get; set; }

    [Range(1, int.MaxValue)]
    public int MemberId { get; set; }
}
