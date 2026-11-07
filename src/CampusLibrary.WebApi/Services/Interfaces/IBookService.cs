using CampusLibrary.WebApi.DTOs;

namespace CampusLibrary.WebApi.Services.Interfaces;

public interface IBookService
{
    Task<List<BookDto>> GetAllBooksAsync();
    Task<BookDto?> GetBookByIdAsync(int id);
    Task<BookDto> CreateBookAsync(CreateBookDto createDto);
    Task UpdateBookAsync(int id, UpdateBookDto updateDto);
    Task DeleteBookAsync(int id);
}
