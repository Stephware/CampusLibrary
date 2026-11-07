using CampusLibrary.WebMvc.ViewModels;

namespace CampusLibrary.WebMvc.Services.Interfaces;

public interface IMemberAdminService
{
    Task<List<MemberViewModel>> GetAllAsync();
    Task CreateAsync(MemberFormViewModel model);
}
