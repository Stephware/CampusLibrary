using CampusLibrary.WebApi.DTOs;

namespace CampusLibrary.WebApi.Services.Interfaces;

public interface ILoanService
{
    Task<List<LoanDto>> GetAllLoansAsync(int? memberId = null, bool openOnly = false);
    Task<LoanDto?> GetLoanByIdAsync(int id);
    Task<LoanDto> BorrowBookAsync(CreateLoanDto dto);
    Task ReturnBookAsync(int id);
}
