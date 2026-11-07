using CampusLibrary.Data.Entities;
using CampusLibrary.Data.Repositories.Interfaces;
using CampusLibrary.WebMvc.Services.Interfaces;
using CampusLibrary.WebMvc.ViewModels;

namespace CampusLibrary.WebMvc.Services;

public class BookAdminService : IBookAdminService
{
    private readonly IBookRepository _repository;
    private readonly ILogger<BookAdminService> _logger;

    public BookAdminService(IBookRepository repository, ILogger<BookAdminService> logger)
    {
        _repository = repository;
        _logger = logger;
    }

    public async Task<List<BookViewModel>> GetAllAsync() =>
        (await _repository.GetAllAsync()).OrderBy(b => b.Title).Select(ToModel).ToList();

    public async Task<BookViewModel?> GetByIdAsync(int id)
    {
        var book = await _repository.GetByIdAsync(id);
        return book == null ? null : ToModel(book);
    }

    public async Task CreateAsync(BookFormViewModel model)
    {
        var isbn = model.Isbn.Trim();
        if (await _repository.ExistsByIsbnAsync(isbn))
            throw new InvalidOperationException("A book with this ISBN already exists. Use a different ISBN or edit the existing book.");
        var book = await _repository.AddAsync(new Book
        {
            Title = model.Title.Trim(), Isbn = isbn,
            Author = model.Author?.Trim(), YearPublished = model.YearPublished
        });
        _logger.LogInformation("Admin created book {Id}", book.Id);
    }

    public async Task UpdateAsync(int id, BookFormViewModel model)
    {
        var book = await _repository.GetByIdAsync(id)
            ?? throw new KeyNotFoundException("Book not found.");
        var isbn = model.Isbn.Trim();
        if (await _repository.ExistsByIsbnAsync(isbn, id))
            throw new InvalidOperationException("A book with this ISBN already exists. Use a different ISBN or edit the existing book.");
        book.Title = model.Title.Trim();
        book.Isbn = isbn;
        book.Author = model.Author?.Trim();
        book.YearPublished = model.YearPublished;
        await _repository.UpdateAsync(book);
        _logger.LogInformation("Admin updated book {Id}", id);
    }

    public async Task DeleteAsync(int id)
    {
        if (await _repository.GetByIdAsync(id) == null)
            throw new KeyNotFoundException("Book not found.");
        await _repository.DeleteAsync(id);
        _logger.LogInformation("Admin deleted book {Id}", id);
    }

    private static BookViewModel ToModel(Book book) => new()
    {
        Id = book.Id, Title = book.Title, Isbn = book.Isbn,
        Author = book.Author, YearPublished = book.YearPublished
    };
}
