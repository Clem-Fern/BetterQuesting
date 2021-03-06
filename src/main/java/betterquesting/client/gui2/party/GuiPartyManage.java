package betterquesting.client.gui2.party;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.client.gui.misc.INeedsRefresh;
import betterquesting.api.enums.EnumPacketAction;
import betterquesting.api.enums.EnumPartyStatus;
import betterquesting.api.network.QuestingPacket;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.party.IParty;
import betterquesting.api.utils.BigItemStack;
import betterquesting.api.utils.RenderUtils;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.*;
import betterquesting.api2.client.gui.panels.CanvasEmpty;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelPlayerPortrait;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.resources.textures.ItemTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetIcon;
import betterquesting.api2.client.gui.themes.presets.PresetLine;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import betterquesting.core.BetterQuesting;
import betterquesting.network.PacketSender;
import betterquesting.network.PacketTypeNative;
import betterquesting.questing.party.PartyManager;
import betterquesting.storage.LifeDatabase;
import betterquesting.storage.NameCache;
import betterquesting.storage.QuestSettings;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;

import java.util.List;
import java.util.UUID;

public class GuiPartyManage extends GuiScreenCanvas implements IPEventListener, INeedsRefresh
{
    private IParty party;
	private EnumPartyStatus status; // 0 = INVITE, 1 = MEMBER, 2 = ADMIN, 3 = OWNER/OP
    private PanelTextField<String> flName;
    private PanelVScrollBar scUserList;
    
    public GuiPartyManage(GuiScreen parent)
    {
        super(parent);
    }
    
    @Override
    public void refreshGui()
    {
        UUID playerID = QuestingAPI.getQuestingUUID(mc.player);
        
        this.party = PartyManager.INSTANCE.getUserParty(playerID);
        
        if(party == null)
        {
            mc.displayGuiScreen(new GuiPartyCreate(parent));
            return;
        }
        
        if(!flName.isFocused()) flName.setText(party.getName());
        
        initPanel();
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
    
        UUID playerID = QuestingAPI.getQuestingUUID(mc.player);
        
        this.party = PartyManager.INSTANCE.getUserParty(playerID);
        
        if(party == null)
        {
            mc.displayGuiScreen(new GuiPartyCreate(parent));
            return;
        }
    
        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
		Keyboard.enableRepeatEvents(true);
		
		status = NameCache.INSTANCE.isOP(playerID)? EnumPartyStatus.OWNER : party.getStatus(playerID);
    
        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
    
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, QuestTranslation.translate("gui.back")));
    
        PanelTextBox txTitle = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), QuestTranslation.translate("betterquesting.title.party", party.getName())).setAlignment(1);
        txTitle.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(txTitle);
        
        // Left side
        
        CanvasEmpty cvLeftHalf = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_LEFT, new GuiPadding(16, 64, 8, 64), 0));
        cvBackground.addPanel(cvLeftHalf);
        
        PanelButton btnLifeShare = new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, -75, 0, 150, 16, 0), 1,QuestTranslation.translate("betterquesting.btn.party_share_lives") + ": " + party.getProperties().getProperty(NativeProps.PARTY_LIVES));
        cvLeftHalf.addPanel(btnLifeShare);
        btnLifeShare.setActive(status.ordinal() >= 3);
        
        PanelButtonStorage<UUID> btnLeave = new PanelButtonStorage<>(new GuiTransform(GuiAlign.MID_CENTER, -75, 32, 70, 16, 0), 3, QuestTranslation.translate("betterquesting.btn.party_leave"), playerID);
        cvLeftHalf.addPanel(btnLeave);
        
        PanelButton btnInvite = new PanelButton(new GuiTransform(GuiAlign.MID_CENTER, 5, 32, 70, 16, 0), 2, QuestTranslation.translate("betterquesting.btn.party_invite"));
        cvLeftHalf.addPanel(btnInvite);
        btnInvite.setActive(status.ordinal() >= 2);
        
        if(flName == null) flName = new PanelTextField<>(new GuiTransform(GuiAlign.MID_CENTER, -75, -32, 134, 16, 0), party.getName(), FieldFilterString.INSTANCE);
        cvLeftHalf.addPanel(flName);
        flName.setActive(status.ordinal() >= 3);
        
        PanelButton btnSetName = new PanelButton(new GuiTransform(GuiAlign.RIGHT_EDGE, 0, 0, 16, 16, 0), 4, "");
        cvLeftHalf.addPanel(btnSetName);
        btnSetName.getTransform().setParent(flName.getTransform());
        btnSetName.setIcon(PresetIcon.ICON_REFRESH.getTexture());
        btnSetName.setActive(status.ordinal() >= 3);
        
        PanelTextBox txName = new PanelTextBox(new GuiTransform(GuiAlign.MID_CENTER, -75, -48, 134, 16, 0), QuestTranslation.translate("betterquesting.gui.name"));
        txName.setColor(PresetColor.TEXT_HEADER.getColor());
        cvLeftHalf.addPanel(txName);
        
        // Right side
        
        CanvasEmpty cvRightHalf = new CanvasEmpty(new GuiTransform(GuiAlign.HALF_RIGHT, new GuiPadding(8, 32, 16, 32), 0));
        cvBackground.addPanel(cvRightHalf);
        
        PanelTextBox txInvite = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 0, 0, -16), 0), QuestTranslation.translate("betterquesting.gui.party_members")).setAlignment(1);
        txInvite.setColor(PresetColor.TEXT_HEADER.getColor());
        cvRightHalf.addPanel(txInvite);
        
        CanvasScrolling cvUserList = new CanvasScrolling(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 16, 8, 0), 0));
        cvRightHalf.addPanel(cvUserList);
    
        if(scUserList == null) scUserList = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(0, 0, -8, 0), 0));
        cvRightHalf.addPanel(scUserList);
        scUserList.getTransform().setParent(cvUserList.getTransform());
        cvUserList.setScrollDriverY(scUserList);
        
        List<UUID> partyMemList = party.getMembers();
        int elSize = RenderUtils.getStringWidth("...", fontRenderer);
        int cvWidth = cvUserList.getTransform().getWidth();
        boolean hardcore = QuestSettings.INSTANCE.getProperty(NativeProps.HARDCORE);
        ItemTexture txHeart = new ItemTexture(new BigItemStack(BetterQuesting.extraLife));
        
        for(int i = 0; i < partyMemList.size(); i++)
        {
            UUID mid = partyMemList.get(i);
            String mName = NameCache.INSTANCE.getName(mid);
            
            if(RenderUtils.getStringWidth(mName, fontRenderer) > cvWidth - 58)
            {
                mName = mc.fontRenderer.trimStringToWidth(mName, cvWidth - 58 - elSize) + "...";
            }
    
            PanelPlayerPortrait pnPortrait = new PanelPlayerPortrait(new GuiRectangle(0, i * 32, 32, 32, 0), mid, mName);
            cvUserList.addPanel(pnPortrait);
            
            PanelTextBox txMemName = new PanelTextBox(new GuiRectangle(32, i * 32 + 4, cvWidth - 32, 12, 0), mName);
            txMemName.setColor(PresetColor.TEXT_MAIN.getColor());
            cvUserList.addPanel(txMemName);
    
            PanelButtonStorage<UUID> btnKick = new PanelButtonStorage<>(new GuiRectangle(cvWidth - 32, i * 32, 32, 32, 0), 3, QuestTranslation.translate("betterquesting.btn.party_kick"), mid);
            cvUserList.addPanel(btnKick);
    
            PanelGeneric pnItem = new PanelGeneric(new GuiRectangle(32, i * 32 + 16, 16, 16, 0), txHeart);
            cvUserList.addPanel(pnItem);
            
            String lifeCount;
            
            if(hardcore)
            {
                lifeCount = " x " + LifeDatabase.INSTANCE.getLives(mid);
            } else
            {
                lifeCount = " x \u221E";
            }
            
            PanelTextBox txLives = new PanelTextBox(new GuiRectangle(48, i * 32 + 20, cvWidth - 48 - 32, 12, 0), lifeCount);
            txLives.setColor(PresetColor.TEXT_MAIN.getColor());
            cvUserList.addPanel(txLives);
        }
        
        scUserList.setActive(cvUserList.getScrollBounds().getHeight() > 0);
        
        // Divider
        
        IGuiRect ls0 = new GuiTransform(GuiAlign.TOP_CENTER, 0, 32, 0, 0, 0);
        ls0.setParent(cvBackground.getTransform());
        IGuiRect le0 = new GuiTransform(GuiAlign.BOTTOM_CENTER, 0, -32, 0, 0, 0);
        le0.setParent(cvBackground.getTransform());
        PanelLine paLine0 = new PanelLine(ls0, le0, PresetLine.GUI_DIVIDER.getLine(), 1, PresetColor.GUI_DIVIDER.getColor(), 1);
        cvBackground.addPanel(paLine0);
    }
    
    @Override
    public void onPanelEvent(PanelEvent event)
    {
        if(event instanceof PEventButton)
        {
            onButtonPress((PEventButton)event);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void onButtonPress(PEventButton event)
    {
        IPanelButton btn = event.getButton();
    
        if(btn.getButtonID() == 0) // Exit
        {
            mc.displayGuiScreen(this.parent);
        } else if(btn.getButtonID() == 1) // Toggle Life Share
        {
			party.getProperties().setProperty(NativeProps.PARTY_LIVES, !party.getProperties().getProperty(NativeProps.PARTY_LIVES));
			SendChanges();
        } else if(btn.getButtonID() == 2) // Invite
        {
			mc.displayGuiScreen(new GuiPartyInvite(this));
        } else if(btn.getButtonID() == 3 && btn instanceof PanelButtonStorage) // Kick/Leave
        {
            UUID playerID = QuestingAPI.getQuestingUUID(mc.player);
            UUID id = ((PanelButtonStorage<UUID>)btn).getStoredValue();
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("action", EnumPacketAction.KICK.ordinal());
			tags.setInteger("partyID", PartyManager.INSTANCE.getID(party));
			tags.setString("target", id.toString());
			PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.PARTY_EDIT.GetLocation(), tags));
			
			if(id.equals(playerID))
            {
                mc.displayGuiScreen(new GuiPartyCreate(parent));
            }
        } else if(btn.getButtonID() == 4) // Change name
        {
            party.getProperties().setProperty(NativeProps.NAME, flName.getRawText());
            SendChanges();
        }
    }
	
	public void SendChanges() // Use this if the name is being edited
	{
		if(status != EnumPartyStatus.OWNER && !NameCache.INSTANCE.isOP(QuestingAPI.getQuestingUUID(mc.player)))
		{
			return; // Not allowed to edit the party (Operators may force edit)
		}
		
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("action", EnumPacketAction.EDIT.ordinal());
		tags.setInteger("partyID", PartyManager.INSTANCE.getID(party));
		tags.setTag("data", party.writeToNBT(new NBTTagCompound()));
		PacketSender.INSTANCE.sendToServer(new QuestingPacket(PacketTypeNative.PARTY_EDIT.GetLocation(), tags));
	}
}
