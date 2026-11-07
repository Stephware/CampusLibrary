using CampusLibrary.Data.Entities;
using CampusLibrary.Data.Repositories.Interfaces;
using Microsoft.Data.SqlClient;
using Microsoft.EntityFrameworkCore;

namespace CampusLibrary.Data.Repositories;

public class LoanRepository : ILoanRepository
{
    private readonly LibraryDbContext _context;

    public LoanRepository(LibraryDbContext context)
    {
        _context = context;
    }

    public Task<List<Loan>> GetAllAsync(int? memberId = null, bool openOnly = false)
    {
        var query = _context.Loans.AsNoTracking();
        if (memberId.HasValue)
            query = query.Where(l => l.MemberId == memberId.Value);
        if (openOnly)
            query = query.Where(l => l.ReturnDate == null);
        return query.OrderBy(l => l.Id).ToListAsync();
    }

    public Task<List<Loan>> GetOpenWithDetailsAsync() =>
        _context.Loans.AsNoTracking()
            .Where(l => l.ReturnDate == null)
            .Include(l => l.Book).Include(l => l.Member)
            .OrderBy(l => l.DueDate).ThenBy(l => l.Id)
            .ToListAsync();

    public Task<Loan?> GetByIdAsync(int id) =>
        _context.Loans.AsNoTracking().FirstOrDefaultAsync(l => l.Id == id);

    public Task<bool> HasOpenLoanAsync(int bookId) =>
        _context.Loans.AnyAsync(l => l.BookId == bookId && l.ReturnDate == null);

    public async Task<Loan> AddAsync(Loan loan)
    {
        _context.Loans.Add(loan);
        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateException ex) when (ex.InnerException is SqlException { Number: 2601 or 2627 })
        {
            throw new InvalidOperationException("This book is already on loan.", ex);
        }
        catch (DbUpdateException ex) when (ex.InnerException is SqlException { Number: 547 })
        {
            throw new KeyNotFoundException("The book or member no longer exists.", ex);
        }
        return loan;
    }

    public async Task<bool> ReturnAsync(int id, DateTime returnDate)
    {
        // Only the first return may set the date, even across concurrent requests.
        var updated = await _context.Loans
            .Where(l => l.Id == id && l.ReturnDate == null)
            .ExecuteUpdateAsync(setters => setters.SetProperty(l => l.ReturnDate, returnDate));
        return updated == 1;
    }
}
