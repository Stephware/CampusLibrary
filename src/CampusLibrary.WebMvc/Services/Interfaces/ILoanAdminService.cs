using CampusLibrary.WebMvc.ViewModels;

namespace CampusLibrary.WebMvc.Services.Interfaces;

public interface ILoanAdminService
{
    Task<List<LoanViewModel>> GetOpenAsync();
    Task ReturnAsync(int id);
}
