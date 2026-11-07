using CampusLibrary.WebApi.DTOs;

namespace CampusLibrary.WebApi.Services.Interfaces;

public interface IMemberService
{
    Task<List<MemberDto>> GetAllMembersAsync();
    Task<MemberDto?> GetMemberByIdAsync(int id);
    Task<MemberDto> CreateMemberAsync(CreateMemberDto dto);
    Task UpdateMemberAsync(int id, UpdateMemberDto dto);
    Task DeleteMemberAsync(int id);
}
