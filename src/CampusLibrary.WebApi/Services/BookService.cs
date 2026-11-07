using CampusLibrary.Data.Entities;
using CampusLibrary.Data.Repositories.Interfaces;
using CampusLibrary.WebApi.DTOs;
using CampusLibrary.WebApi.Services.Interfaces;

namespace CampusLibrary.WebApi.Services;

public class BookService : IBookService
{
    private readonly IBookRepository _repository;
    private readonly ILogger<BookService> _logger;

    public BookService(IBookRepository repository, ILogger<BookService> logger)
    {
        _repository = repository;
        _logger = logger;
    }

    public async Task<List<BookDto>> GetAllBooksAsync()
    {
        var books = await _repository.GetAllWithAvailabilityAsync();
        return books.Select(b => ToDto(b.Book, b.IsAvailable)).ToList();
    }

    public async Task<BookDto?> GetBookByIdAsync(int id)
    {
        var book = await _repository.GetByIdWithAvailabilityAsync(id);
        return book == null ? null : ToDto(book.Book, book.IsAvailable);
    }

    public async Task<BookDto> CreateBookAsync(CreateBookDto createDto)
    {
        if (await _repository.ExistsByIsbnAsync(createDto.Isbn.Trim()))
        {
            _logger.LogWarning("Attempted to create book with duplicate ISBN: {Isbn}", createDto.Isbn);
            throw new InvalidOperationException($"A book with ISBN {createDto.Isbn} already exists.");
        }

        var book = new Book
        {
            Title = createDto.Title.Trim(),
            Isbn = createDto.Isbn.Trim(),
            Author = createDto.Author?.Trim(),
            YearPublished = createDto.YearPublished
        };

        var created = await _repository.AddAsync(book);
        _logger.LogInformation("Created book with ID {Id}", created.Id);

        return ToDto(created, isAvailable: true);
    }

    public async Task UpdateBookAsync(int id, UpdateBookDto updateDto)
    {
        var book = await _repository.GetByIdAsync(id);
        if (book == null)
        {
            _logger.LogWarning("Attempted to update non-existent book with ID {Id}", id);
            throw new KeyNotFoundException($"Book with ID {id} not found.");
        }

        if (await _repository.ExistsByIsbnAsync(updateDto.Isbn.Trim(), id))
        {
            _logger.LogWarning("Attempted to update book {Id} with duplicate ISBN: {Isbn}", id, updateDto.Isbn);
            throw new InvalidOperationException($"A book with ISBN {updateDto.Isbn} already exists.");
        }

        book.Title = updateDto.Title.Trim();
        book.Isbn = updateDto.Isbn.Trim();
        book.Author = updateDto.Author?.Trim();
        book.YearPublished = updateDto.YearPublished;

        await _repository.UpdateAsync(book);
        _logger.LogInformation("Updated book with ID {Id}", id);
    }

    private static BookDto ToDto(Book book, bool isAvailable) => new()
    {
        Id = book.Id,
        Title = book.Title,
        Isbn = book.Isbn,
        Author = book.Author,
        YearPublished = book.YearPublished,
        IsAvailable = isAvailable
    };

    public async Task DeleteBookAsync(int id)
    {
        var book = await _repository.GetByIdAsync(id);
        if (book == null)
        {
            _logger.LogWarning("Attempted to delete non-existent book with ID {Id}", id);
            throw new KeyNotFoundException($"Book with ID {id} not found.");
        }

        await _repository.DeleteAsync(id);
        _logger.LogInformation("Deleted book with ID {Id}", id);
    }
}
