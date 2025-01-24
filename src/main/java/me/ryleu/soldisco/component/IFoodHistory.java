package me.ryleu.soldisco.component;

import net.minecraft.world.item.Item;
import org.ladysnake.cca.api.v3.component.Component;

import java.util.Iterator;

public interface IFoodHistory extends Component {
    double getMaxHealth();
    Iterator<Item> iterator();
    boolean add(Item toAdd);
    boolean remove(Item toRemove);
    boolean contains(Item toQuery);
    int size();
    boolean isEmpty();
    void clear();
}
