using CampusLibrary.WebMvc.ViewModels;

namespace CampusLibrary.WebMvc.Services.Interfaces;

public interface IBookAdminService
{
    Task<List<BookViewModel>> GetAllAsync();
    Task<BookViewModel?> GetByIdAsync(int id);
    Task CreateAsync(BookFormViewModel model);
    Task UpdateAsync(int id, BookFormViewModel model);
    Task DeleteAsync(int id);
}
