using CampusLibrary.Data.Entities;

namespace CampusLibrary.Data.Repositories.Interfaces;

public interface IBookRepository
{
    Task<List<Book>> GetAllAsync();
    Task<Book?> GetByIdAsync(int id);
    Task<Book> AddAsync(Book book);
    Task UpdateAsync(Book book);
    Task DeleteAsync(int id);
    Task<bool> ExistsByIsbnAsync(string isbn, int? excludeId = null);
}
