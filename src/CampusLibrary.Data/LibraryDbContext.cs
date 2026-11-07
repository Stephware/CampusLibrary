using CampusLibrary.Data.Entities;
using Microsoft.EntityFrameworkCore;

namespace CampusLibrary.Data;

public class LibraryDbContext : DbContext
{
    public LibraryDbContext(DbContextOptions<LibraryDbContext> options)
        : base(options) { }

    public DbSet<Book> Books => Set<Book>();
    public DbSet<Member> Members => Set<Member>();
    public DbSet<Loan> Loans => Set<Loan>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        // Book configuration
        modelBuilder.Entity<Book>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Title).IsRequired().HasMaxLength(200);
            entity.Property(e => e.Isbn).IsRequired().HasMaxLength(20);
            entity.HasIndex(e => e.Isbn).IsUnique();
            entity.Property(e => e.Author).HasMaxLength(100);
        });

        // Member configuration
        modelBuilder.Entity<Member>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Name).IsRequired().HasMaxLength(100);
            entity.Property(e => e.Email).IsRequired().HasMaxLength(100);
            entity.HasIndex(e => e.Email).IsUnique();
            entity.Property(e => e.StudentId).IsRequired().HasMaxLength(20);
            entity.HasIndex(e => e.StudentId).IsUnique();
        });

        // Loan configuration
        modelBuilder.Entity<Loan>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.HasIndex(e => e.BookId).IsUnique().HasFilter("[ReturnDate] IS NULL");
            entity.HasOne(e => e.Book)
                  .WithMany(b => b.Loans)
                  .HasForeignKey(e => e.BookId)
                  .OnDelete(DeleteBehavior.Restrict);
            entity.HasOne(e => e.Member)
                  .WithMany(m => m.Loans)
                  .HasForeignKey(e => e.MemberId)
                  .OnDelete(DeleteBehavior.Restrict);
        });

        // Seed data - 5 books
        modelBuilder.Entity<Book>().HasData(
            new Book { Id = 1, Title = "Clean Code", Isbn = "978-0132350884", Author = "Robert C. Martin", YearPublished = 2008 },
            new Book { Id = 2, Title = "The Pragmatic Programmer", Isbn = "978-0201616224", Author = "Andrew Hunt", YearPublished = 1999 },
            new Book { Id = 3, Title = "Design Patterns", Isbn = "978-0201633610", Author = "Erich Gamma", YearPublished = 1994 },
            new Book { Id = 4, Title = "Introduction to Algorithms", Isbn = "978-0262033848", Author = "Thomas H. Cormen", YearPublished = 2009 },
            new Book { Id = 5, Title = "Code Complete", Isbn = "978-0735619678", Author = "Steve McConnell", YearPublished = 2004 }
        );

        // Seed data - 3 members
        modelBuilder.Entity<Member>().HasData(
            new Member { Id = 1, Name = "Alice Johnson", Email = "alice.johnson@campus.edu", StudentId = "S001", RegisteredDate = new DateTime(2026, 1, 15) },
            new Member { Id = 2, Name = "Bob Smith", Email = "bob.smith@campus.edu", StudentId = "S002", RegisteredDate = new DateTime(2026, 2, 20) },
            new Member { Id = 3, Name = "Carol White", Email = "carol.white@campus.edu", StudentId = "S003", RegisteredDate = new DateTime(2026, 3, 10) }
        );
    }
}
