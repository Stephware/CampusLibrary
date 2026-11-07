using CampusLibrary.Data.Entities;

namespace CampusLibrary.Data.Repositories.Interfaces;

public interface ILoanRepository
{
    Task<List<Loan>> GetAllAsync(int? memberId = null, bool openOnly = false);
    Task<List<Loan>> GetOpenWithDetailsAsync();
    Task<Loan?> GetByIdAsync(int id);
    Task<bool> HasOpenLoanAsync(int bookId);
    Task<Loan> AddAsync(Loan loan);
    Task<bool> ReturnAsync(int id, DateTime returnDate);
}
