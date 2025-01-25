package me.ryleu.soldisco.component;

import net.minecraft.world.item.Item;
import org.ladysnake.cca.api.v3.component.Component;

import java.util.Iterator;
import java.util.stream.Stream;

public interface IFoodHistory extends Component {
    double getMaxHealth();
    void sync();
    Iterator<Item> iterator();
    Stream<Item> stream();
    boolean add(Item toAdd);
    boolean add(Item toAdd, boolean sync);
    boolean remove(Item toRemove);
    boolean remove(Item toRemove, boolean sync);
    boolean contains(Item toQuery);
    int size();
    boolean isEmpty();
    void clear();
}
