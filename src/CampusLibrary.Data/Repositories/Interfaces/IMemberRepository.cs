using CampusLibrary.Data.Entities;

namespace CampusLibrary.Data.Repositories.Interfaces;

public interface IMemberRepository
{
    Task<List<Member>> GetAllAsync();
    Task<Member?> GetByIdAsync(int id);
    Task<bool> ExistsByEmailOrStudentIdAsync(string email, string studentId, int? excludeId = null);
    Task<bool> HasLoansAsync(int id);
    Task<Member> AddAsync(Member member);
    Task UpdateAsync(Member member);
    Task DeleteAsync(Member member);
}
