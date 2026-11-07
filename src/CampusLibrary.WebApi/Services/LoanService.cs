using CampusLibrary.Data.Entities;
using CampusLibrary.Data.Repositories.Interfaces;
using CampusLibrary.WebApi.DTOs;
using CampusLibrary.WebApi.Services.Interfaces;

namespace CampusLibrary.WebApi.Services;

public class LoanService : ILoanService
{
    private readonly ILoanRepository _repository;
    private readonly IBookRepository _books;
    private readonly IMemberRepository _members;
    private readonly ILogger<LoanService> _logger;

    public LoanService(ILoanRepository repository, IBookRepository books,
        IMemberRepository members, ILogger<LoanService> logger)
    {
        _repository = repository;
        _books = books;
        _members = members;
        _logger = logger;
    }

    public async Task<List<LoanDto>> GetAllLoansAsync(int? memberId = null, bool openOnly = false) =>
        (await _repository.GetAllAsync(memberId, openOnly)).Select(ToDto).ToList();

    public async Task<LoanDto?> GetLoanByIdAsync(int id)
    {
        var loan = await _repository.GetByIdAsync(id);
        return loan == null ? null : ToDto(loan);
    }

    public async Task<LoanDto> BorrowBookAsync(CreateLoanDto dto)
    {
        var book = await _books.GetByIdAsync(dto.BookId)
            ?? throw new KeyNotFoundException("Book not found.");
        if (await _members.GetByIdAsync(dto.MemberId) == null)
            throw new KeyNotFoundException("Member not found.");
        if (await _repository.HasOpenLoanAsync(dto.BookId))
            throw new InvalidOperationException("This book is already on loan.");

        var now = DateTime.UtcNow;
        var loan = await _repository.AddAsync(new Loan
        {
            BookId = dto.BookId, MemberId = dto.MemberId,
            BorrowDate = now, DueDate = now.AddDays(14)
        });
        _logger.LogInformation("Created loan {Id} for book {BookId}", loan.Id, loan.BookId);
        return ToDto(loan, book);
    }

    public async Task ReturnBookAsync(int id)
    {
        var loan = await _repository.GetByIdAsync(id)
            ?? throw new KeyNotFoundException("Loan not found.");
        if (loan.ReturnDate != null || !await _repository.ReturnAsync(id, DateTime.UtcNow))
            throw new InvalidOperationException("This loan has already been returned.");
        _logger.LogInformation("Returned loan with ID {Id}", id);
    }

    private static LoanDto ToDto(Loan loan, Book? book = null) => new()
    {
        Id = loan.Id, BookId = loan.BookId, MemberId = loan.MemberId,
        BookTitle = (book ?? loan.Book)?.Title,
        BookAuthor = (book ?? loan.Book)?.Author,
        BorrowDate = DateTime.SpecifyKind(loan.BorrowDate, DateTimeKind.Utc),
        DueDate = DateTime.SpecifyKind(loan.DueDate, DateTimeKind.Utc),
        ReturnDate = loan.ReturnDate.HasValue
            ? DateTime.SpecifyKind(loan.ReturnDate.Value, DateTimeKind.Utc) : null
    };
}
