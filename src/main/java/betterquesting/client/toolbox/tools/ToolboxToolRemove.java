package betterquesting.client.toolbox.tools;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.client.gui2.editors.designer.PanelToolController;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.QuestLineDatabase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ToolboxToolRemove implements IToolboxTool
{
	private CanvasQuestLine gui;
	
	@Override
	public void initTool(CanvasQuestLine gui)
	{
		this.gui = gui;
	}
	
	@Override
	public void disableTool()
	{
	}
	
	@Override
    public void refresh(CanvasQuestLine gui)
    {
    }
	
	@Override
	public boolean onMouseClick(int mx, int my, int click)
	{
		if(click != 0 || !gui.getTransform().contains(mx, my))
		{
			return false;
		}
		
		IQuestLine line = gui.getQuestLine();
		PanelButtonQuest btn = gui.getButtonAt(mx, my);
		
		if(line != null && btn != null)
		{
		    if(PanelToolController.selected.size() > 0)
            {
                if(!PanelToolController.selected.contains(btn)) return false;
                for(PanelButtonQuest b : PanelToolController.selected) line.removeID(b.getStoredValue().getID());
            } else
            {
                int qID = btn.getStoredValue().getID();
                line.removeID(qID);
            }
			
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", EnumPacketAction.EDIT.ordinal());
			NBTTagCompound base = new NBTTagCompound();
			base.setTag("line", line.writeToNBT(new NBTTagCompound(), null));
			tags.setTag("data", base);
			tags.setInteger("lineID", QuestLineDatabase.INSTANCE.getID(line));
			PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.LINE_EDIT.GetLocation(), tags));
			return true;
		}
		
		return false;
	}
	
	@Override
    public boolean onMouseRelease(int mx, int my, int click)
    {
        return false;
    }

	@Override
	public void drawCanvas(int mx, int my, float partialTick)
	{
	}
	
	@Override
    public void drawOverlay(int mx, int my, float partialTick)
    {
    }
    
    @Override
    public List<String> getTooltip(int mx, int my)
    {
        return null;
    }

	@Override
	public boolean onMouseScroll(int mx, int my, int scroll)
	{
	    return false;
	}

	@Override
	public boolean onKeyPressed(char c, int key)
	{
	    if(PanelToolController.selected.size() > 0 && key == Keyboard.KEY_RETURN)
        {
            IQuestLine line = gui.getQuestLine();
            for(PanelButtonQuest b : PanelToolController.selected) line.removeID(b.getStoredValue().getID());
			
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", EnumPacketAction.EDIT.ordinal());
			NBTTagCompound base = new NBTTagCompound();
			base.setTag("line", line.writeToNBT(new NBTTagCompound(), null));
			tags.setTag("data", base);
			tags.setInteger("lineID", QuestLineDatabase.INSTANCE.getID(line));
			PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.LINE_EDIT.GetLocation(), tags));
			return true;
        }
        
	    return false;
	}

	@Override
	public boolean clampScrolling()
	{
		return true;
	}
	
	@Override
    public void onSelection(NonNullList<PanelButtonQuest> buttons)
    {
    }
	
	@Override
    public boolean useSelection()
    {
        return true;
    }
}
