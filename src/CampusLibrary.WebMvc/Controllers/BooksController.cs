using CampusLibrary.WebMvc.Services.Interfaces;
using CampusLibrary.WebMvc.ViewModels;
using Microsoft.AspNetCore.Mvc;

namespace CampusLibrary.WebMvc.Controllers;

public class BooksController : Controller
{
    private readonly IBookAdminService _service;

    public BooksController(IBookAdminService service)
    {
        _service = service;
    }

    [HttpGet]
    public async Task<IActionResult> Index() => View(await _service.GetAllAsync());

    [HttpGet]
    public IActionResult Create() => View(new BookFormViewModel());

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Create(BookFormViewModel model)
    {
        if (!ModelState.IsValid) return View(model);
        try
        {
            await _service.CreateAsync(model);
            TempData["Success"] = "Book added to the catalog.";
            return RedirectToAction(nameof(Index));
        }
        catch (InvalidOperationException ex)
        {
            ModelState.AddModelError("", ex.Message);
            return View(model);
        }
    }

    [HttpGet]
    public async Task<IActionResult> Edit([FromRoute] int id)
    {
        var book = await _service.GetByIdAsync(id);
        return book == null ? NotFound() : View(book);
    }

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Edit([FromRoute] int id, BookFormViewModel model)
    {
        if (!ModelState.IsValid) return View(model);
        try
        {
            await _service.UpdateAsync(id, model);
            TempData["Success"] = "Book details saved.";
            return RedirectToAction(nameof(Index));
        }
        catch (KeyNotFoundException) { return NotFound(); }
        catch (InvalidOperationException ex)
        {
            ModelState.AddModelError("", ex.Message);
            return View(model);
        }
    }

    [HttpGet]
    public async Task<IActionResult> Delete([FromRoute] int id)
    {
        var book = await _service.GetByIdAsync(id);
        return book == null ? NotFound() : View(book);
    }

    [HttpPost, ActionName("Delete"), ValidateAntiForgeryToken]
    public async Task<IActionResult> DeleteConfirmed([FromRoute] int id)
    {
        try
        {
            await _service.DeleteAsync(id);
            TempData["Success"] = "Book deleted from the catalog.";
            return RedirectToAction(nameof(Index));
        }
        catch (KeyNotFoundException) { return NotFound(); }
        catch (InvalidOperationException ex)
        {
            var book = await _service.GetByIdAsync(id);
            if (book == null) return NotFound();
            ModelState.AddModelError("", ex.Message);
            return View("Delete", book);
        }
    }
}
