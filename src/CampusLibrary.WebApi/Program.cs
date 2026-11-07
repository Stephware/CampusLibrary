using CampusLibrary.WebApi.Infrastructure;
using CampusLibrary.Data;
using CampusLibrary.Data.Repositories;
using CampusLibrary.Data.Repositories.Interfaces;
using CampusLibrary.WebApi.Services;
using CampusLibrary.WebApi.Services.Interfaces;
using Microsoft.EntityFrameworkCore;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
builder.Services.AddDbContext<LibraryDbContext>(options =>
    options.UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection")));

// Register repositories
builder.Services.AddScoped<IBookRepository, BookRepository>();
builder.Services.AddScoped<IMemberRepository, MemberRepository>();
builder.Services.AddScoped<ILoanRepository, LoanRepository>();

// Register services
builder.Services.AddScoped<IBookService, BookService>();
builder.Services.AddScoped<IMemberService, MemberService>();
builder.Services.AddScoped<ILoanService, LoanService>();
builder.Services.AddProblemDetails();
builder.Services.AddExceptionHandler<ApiExceptionHandler>();

// Add controllers
builder.Services.AddControllers();

// Configure CORS for Android emulator
builder.Services.AddCors(options =>
{
    options.AddPolicy("AndroidEmulator", policy =>
    {
        policy.WithOrigins("http://10.0.2.2")
              .AllowAnyHeader()
              .AllowAnyMethod();
    });
});

// Learn more about configuring OpenAPI at https://aka.ms/aspnet/openapi
builder.Services.AddOpenApi();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

app.UseExceptionHandler();

app.UseCors("AndroidEmulator");

app.UseHttpsRedirection();

app.MapControllers();

app.Run();
