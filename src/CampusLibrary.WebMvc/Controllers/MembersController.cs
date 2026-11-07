using CampusLibrary.WebMvc.Services.Interfaces;
using CampusLibrary.WebMvc.ViewModels;
using Microsoft.AspNetCore.Mvc;

namespace CampusLibrary.WebMvc.Controllers;

public class MembersController : Controller
{
    private readonly IMemberAdminService _service;

    public MembersController(IMemberAdminService service)
    {
        _service = service;
    }

    [HttpGet]
    public async Task<IActionResult> Index() => View(await _service.GetAllAsync());

    [HttpGet]
    public IActionResult Create() => View(new MemberFormViewModel());

    [HttpPost, ValidateAntiForgeryToken]
    public async Task<IActionResult> Create(MemberFormViewModel model)
    {
        if (!ModelState.IsValid) return View(model);
        try
        {
            await _service.CreateAsync(model);
            TempData["Success"] = "Member registered. They can now borrow books.";
            return RedirectToAction(nameof(Index));
        }
        catch (InvalidOperationException ex)
        {
            ModelState.AddModelError("", ex.Message);
            return View(model);
        }
    }
}
