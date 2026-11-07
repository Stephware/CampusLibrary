using CampusLibrary.Data.Repositories.Interfaces;
using CampusLibrary.WebMvc.Services.Interfaces;
using CampusLibrary.WebMvc.ViewModels;

namespace CampusLibrary.WebMvc.Services;

public class LoanAdminService : ILoanAdminService
{
    private readonly ILoanRepository _repository;
    private readonly ILogger<LoanAdminService> _logger;

    public LoanAdminService(ILoanRepository repository, ILogger<LoanAdminService> logger)
    {
        _repository = repository;
        _logger = logger;
    }

    public async Task<List<LoanViewModel>> GetOpenAsync() =>
        (await _repository.GetOpenWithDetailsAsync()).Select(l => new LoanViewModel
        {
            Id = l.Id, BookTitle = l.Book.Title, Isbn = l.Book.Isbn,
            MemberName = l.Member.Name, StudentId = l.Member.StudentId,
            BorrowDate = l.BorrowDate, DueDate = l.DueDate
        }).ToList();

    public async Task ReturnAsync(int id)
    {
        var loan = await _repository.GetByIdAsync(id)
            ?? throw new KeyNotFoundException("Loan not found. It may have been removed.");
        if (loan.ReturnDate != null || !await _repository.ReturnAsync(id, DateTime.UtcNow))
            throw new InvalidOperationException("This loan has already been returned. The open loans list is up to date.");
        _logger.LogInformation("Admin returned loan {Id}", id);
    }
}
