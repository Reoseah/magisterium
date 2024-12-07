package io.github.reoseah.magisterium.data;

import io.github.reoseah.magisterium.magisterium.book.Book;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Map;

public class BookLoader extends JsonDataLoader<Book> implements IdentifiableResourceReloadListener {
    public static final Identifier ID = Identifier.of("magisterium", "book");

    public static BookLoader instance;

    public Map<Identifier, Book> books;

    public BookLoader(RegistryWrapper.WrapperLookup registries) {
        super(registries, Book.CODEC.codec(), "magisterium/books");
        instance = this;
    }

    public BookLoader() {
        super(Book.CODEC.codec(), "magisterium/books");
        instance = this;
    }

    public static BookLoader getInstance() {
        return instance;
    }

    public static void setClientSide(Map<Identifier, Book> books) {
        instance = new BookLoader();
        instance.books = books;
    }

    public static void disconnectClientSide() {
        instance = null;
    }

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    protected void apply(Map<Identifier, Book> prepared, ResourceManager manager, Profiler profiler) {
        this.books = prepared;
    }
}
