using System.ComponentModel.DataAnnotations;
using CampusLibrary.WebApi.DTOs;
using CampusLibrary.WebApi.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace CampusLibrary.WebApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class LoansController : ControllerBase
{
    private readonly ILoanService _loanService;

    public LoansController(ILoanService loanService)
    {
        _loanService = loanService;
    }

    [HttpGet]
    public async Task<ActionResult<List<LoanDto>>> GetAll(
        [FromQuery, Range(1, int.MaxValue)] int? memberId = null,
        [FromQuery] bool openOnly = false) =>
        Ok(await _loanService.GetAllLoansAsync(memberId, openOnly));

    [HttpGet("{id}")]
    public async Task<ActionResult<LoanDto>> GetById([Range(1, int.MaxValue)] int id)
    {
        var loan = await _loanService.GetLoanByIdAsync(id);
        return loan == null ? NotFound() : Ok(loan);
    }

    [HttpPost]
    public async Task<ActionResult<LoanDto>> Borrow(CreateLoanDto dto)
    {
        var loan = await _loanService.BorrowBookAsync(dto);
        return CreatedAtAction(nameof(GetById), new { id = loan.Id }, loan);
    }

    [HttpPost("{id}/return")]
    public async Task<IActionResult> Return([Range(1, int.MaxValue)] int id)
    {
        await _loanService.ReturnBookAsync(id);
        return NoContent();
    }
}
