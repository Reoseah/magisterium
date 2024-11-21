package io.github.reoseah.magisterium.data;

import io.github.reoseah.magisterium.data.book.BookData;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Map;

public class BookLoader extends JsonDataLoader<BookData> implements IdentifiableResourceReloadListener {
    public static final Identifier ID = Identifier.of("magisterium", "book");

    public static BookLoader instance;

    public Map<Identifier, BookData> books;

    public BookLoader(RegistryWrapper.WrapperLookup registries) {
        super(registries, BookData.CODEC.codec(), "magisterium/books");
        instance = this;
    }

    public BookLoader() {
        super(BookData.CODEC.codec(), "magisterium/books");
        instance = this;
    }

    public static BookLoader getInstance() {
        return instance;
    }

    public static void setClientSide(Map<Identifier, BookData> books) {
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
    protected void apply(Map<Identifier, BookData> prepared, ResourceManager manager, Profiler profiler) {
        this.books = prepared;
    }
}
