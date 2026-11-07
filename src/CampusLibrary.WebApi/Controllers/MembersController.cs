using System.ComponentModel.DataAnnotations;
using CampusLibrary.WebApi.DTOs;
using CampusLibrary.WebApi.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace CampusLibrary.WebApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class MembersController : ControllerBase
{
    private readonly IMemberService _memberService;

    public MembersController(IMemberService memberService)
    {
        _memberService = memberService;
    }

    [HttpGet]
    public async Task<ActionResult<List<MemberDto>>> GetAll() =>
        Ok(await _memberService.GetAllMembersAsync());

    [HttpGet("{id}")]
    public async Task<ActionResult<MemberDto>> GetById([Range(1, int.MaxValue)] int id)
    {
        var member = await _memberService.GetMemberByIdAsync(id);
        return member == null ? NotFound() : Ok(member);
    }

    [HttpPost]
    public async Task<ActionResult<MemberDto>> Create(CreateMemberDto dto)
    {
        var member = await _memberService.CreateMemberAsync(dto);
        return CreatedAtAction(nameof(GetById), new { id = member.Id }, member);
    }

    [HttpPut("{id}")]
    public async Task<IActionResult> Update([Range(1, int.MaxValue)] int id, UpdateMemberDto dto)
    {
        await _memberService.UpdateMemberAsync(id, dto);
        return NoContent();
    }

    [HttpDelete("{id}")]
    public async Task<IActionResult> Delete([Range(1, int.MaxValue)] int id)
    {
        await _memberService.DeleteMemberAsync(id);
        return NoContent();
    }
}
