using CampusLibrary.WebMvc.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace CampusLibrary.WebMvc.Controllers;

public class LoansController : Controller
{
    private readonly ILoanAdminService _service;

    public LoansController(ILoanAdminService service)
    {
        _service = service;
    }

    [HttpGet]
    public async Task<IActionResult> Index() => View(await _service.GetOpenAsync());

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Return([FromRoute] int id)
    {
        try
        {
            await _service.ReturnAsync(id);
            TempData["Success"] = "Book returned. It is available to borrow again.";
        }
        catch (KeyNotFoundException) { return NotFound(); }
        catch (InvalidOperationException ex) { TempData["Notice"] = ex.Message; }
        return RedirectToAction(nameof(Index));
    }
}
