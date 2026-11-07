using CampusLibrary.Data.Entities;
using CampusLibrary.Data.Repositories.Interfaces;
using Microsoft.Data.SqlClient;
using Microsoft.EntityFrameworkCore;

namespace CampusLibrary.Data.Repositories;

public class MemberRepository : IMemberRepository
{
    private readonly LibraryDbContext _context;

    public MemberRepository(LibraryDbContext context)
    {
        _context = context;
    }

    public Task<List<Member>> GetAllAsync() =>
        _context.Members.AsNoTracking().OrderBy(m => m.Id).ToListAsync();

    public Task<Member?> GetByIdAsync(int id) =>
        _context.Members.FirstOrDefaultAsync(m => m.Id == id);

    public Task<bool> ExistsByEmailOrStudentIdAsync(string email, string studentId, int? excludeId = null) =>
        _context.Members.AnyAsync(m => (!excludeId.HasValue || m.Id != excludeId.Value)
            && (m.Email == email || m.StudentId == studentId));

    public Task<bool> HasLoansAsync(int id) =>
        _context.Loans.AnyAsync(l => l.MemberId == id);

    public async Task<Member> AddAsync(Member member)
    {
        _context.Members.Add(member);
        await SaveAsync();
        return member;
    }

    public Task UpdateAsync(Member member) => SaveAsync();

    public async Task DeleteAsync(Member member)
    {
        _context.Members.Remove(member);
        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateConcurrencyException ex)
        {
            throw new KeyNotFoundException("Member no longer exists.", ex);
        }
        catch (DbUpdateException ex) when (ex.InnerException is SqlException { Number: 547 })
        {
            throw new InvalidOperationException("A member with loan history cannot be deleted.", ex);
        }
    }

    private async Task SaveAsync()
    {
        try
        {
            await _context.SaveChangesAsync();
        }
        catch (DbUpdateConcurrencyException ex)
        {
            throw new KeyNotFoundException("Member no longer exists.", ex);
        }
        catch (DbUpdateException ex) when (ex.InnerException is SqlException { Number: 2601 or 2627 })
        {
            // The unique indexes also protect concurrent member writes.
            throw new InvalidOperationException("A member with this email or student ID already exists.", ex);
        }
    }
}
