package io.github.reoseah.magisterium.magisterium.page;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.reoseah.magisterium.magisterium.page.element.PageElement;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.List;

public class SpellPage {
    public static final MapCodec<SpellPage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group( //
            PageElement.CODEC.listOf().fieldOf("elements").forGetter(page -> page.elements) //
    ).apply(instance, SpellPage::new));
    public static final PacketCodec<RegistryByteBuf, SpellPage> PACKET_CODEC = PacketCodecs.registryCodec(CODEC.codec());

    public final List<PageElement> elements;

    public SpellPage(List<PageElement> elements) {
        this.elements = elements;
    }
}
