package guichaguri.betterfps.gui;

import guichaguri.betterfps.BetterFpsConfig;
import guichaguri.betterfps.BetterFpsHelper;
import guichaguri.betterfps.UpdateChecker;
import guichaguri.betterfps.gui.GuiCycleButton.GuiBooleanButton;
import guichaguri.betterfps.tweaker.BetterFpsTweaker;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Util;
import net.minecraft.util.Util.EnumOS;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.input.Mouse;

/**
 * @author Guilherme Chaguri
 */
public class GuiBetterFpsConfig extends GuiScreen {

    private GuiScreen parent = null;
    public GuiBetterFpsConfig() {}
    public GuiBetterFpsConfig(GuiScreen parent) {
        this.parent = parent;
    }


    private List<GuiButton> initButtons() {
        List<GuiButton> buttons = new ArrayList<GuiButton>();
        BetterFpsConfig config = BetterFpsConfig.getConfig();
        buttons.add(new AlgorithmButton(2, "Algorithm", BetterFpsHelper.displayHelpers,
                config.algorithm, new String[] {
                        "The algorithm for calculating sine and cosine",
                        "§cRequires restarting to take effect", "",
                        "§eShift-click me to test algorithms §7(This will take a few seconds)", "",
                        "§aMore information here soon", "",
                        "Default in Vanilla: Vanilla Algorithm",
                        "Default in BetterFps: Riven's \"Half\" Algorithm",
        }));
        buttons.add(new UpdateCheckerButton(3, "Update Checker", config.updateChecker, new String[] {
                        "Whether updates will be checked on startup", "",
                        "§eShift-click me to check for updates §7(This will take a few seconds)", "",
                        "Default: On"
        }));
        buttons.add(new GuiBooleanButton(4, "Preallocate Memory", config.preallocateMemory, new String[] {
                        "Whether will preallocate 10MB on startup.",
                        "§cRequires restarting to take effect", "",
                        "Default in Vanilla: On",
                        "Default in BetterFps: Off",
                        "",
                        "Note: This allocation will be cleaned only when the memory is almost full",
                        "Useful for modpacks that require a lot of RAM"
        }));
        buttons.add(new GuiBooleanButton(5, "Fast Box Render", config.fastBoxRender, new String[] {
                        "Whether will only render the exterior of boxes.",
                        "§cRequires restarting to take effect", "",
                        "Default in Vanilla: Off",
                        "Default in BetterFps: On"
        }));
        buttons.add(new GuiBooleanButton(6, "Fog", config.fog, new String[] {
                        "Whether fog will be rendered.",
                        "§cRequires restarting to take effect", "",
                        "Default: On"
        }));
        buttons.add(new GuiBooleanButton(7, "Fast Hopper", config.fastHopper, new String[] {
                        "Whether hopper improvements will be enabled.",
                        "§cRequires restarting to take effect", "",
                        "Default in Vanilla: Off",
                        "Default in BetterFps: On"
        }));
        buttons.add(new GuiBooleanButton(8, "Fast Beacon", config.fastBeacon, new String[] {
                        "Whether the beacon improvements will be enabled.",
                        "§cRequires restarting to take effect", "",
                        "Default in Vanilla: Off",
                        "Default in BetterFps: On"
        }));
        buttons.add(new GuiBooleanButton(9, "Fast Beacon Rendering", config.fastBeaconRender, new String[] {
                "Whether the beacon glow will be removed.",
                "§cRequires restarting to take effect", "",
                "Default: Off"
        }));
        return buttons;
    }

    @Override
    public void initGui() {
        int x1 = width / 2 - 155;
        int x2 = width / 2 + 5;

        buttonList.clear();
        buttonList.add(new GuiButton(-1, x1, height - 27, 150, 20, I18n.format("gui.done")));
        buttonList.add(new GuiButton(-2, x2, height - 27, 150, 20, I18n.format("gui.cancel")));

        List<GuiButton> buttons = initButtons();

        int y = 25;
        int lastId = 0;

        for(GuiButton button : buttons) {
            boolean first = button.id % 2 != 0;
            boolean large = button.id - 1 != lastId;
            button.xPosition = (first || large) ? x1 : x2;
            button.yPosition = y;
            button.setWidth(large ? 310 : 150);
            buttonList.add(button);
            if((!first) || (large)) y += 25;
            lastId = button.id;
        }

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        if(mouseY < fontRendererObj.FONT_HEIGHT + 14) {
            if(Mouse.isButtonDown(1)) {
                drawCenteredString(fontRendererObj, "This is not a button", this.width / 2, 7, 0xC0C0C0);
            } else {
                drawCenteredString(fontRendererObj, "Hold right-click on a button for information", this.width / 2, 7, 0xC0C0C0);
            }
        } else {
            drawCenteredString(fontRendererObj, "BetterFps Options", this.width / 2, 7, 0xFFFFFF);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        if(Mouse.isButtonDown(1)) { // Right Click
            for(GuiButton button : buttonList) {
                if((button instanceof GuiCycleButton) && (button.isMouseOver())) {
                    int y = mouseY + 5;

                    String[] help = ((GuiCycleButton)button).getHelpText();
                    int fontHeight = fontRendererObj.FONT_HEIGHT, i = 0;
                    drawGradientRect(0, y, mc.displayWidth, y + (fontHeight * help.length) + 10, -1072689136, -804253680);
                    for(String h : help) {
                        if(!h.isEmpty()) fontRendererObj.drawString(h, 5, y + (i * fontHeight) + 5, 0xFFFFFF);
                        i++;
                    }
                    break;
                }
            }
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        if(button instanceof GuiCycleButton) {
            ((GuiCycleButton)button).actionPerformed();
        } else if(button.id == -1) {
            // Save
            boolean restart = false;
            BetterFpsConfig config = BetterFpsConfig.getConfig();

            GuiCycleButton algorithmButton = getCycleButton(2);
            String algorithm = algorithmButton.getSelectedValue();
            if(!algorithm.equals(config.algorithm)) restart = true;

            config.algorithm = algorithm;

            GuiCycleButton updateButton = getCycleButton(3);
            config.updateChecker = updateButton.getSelectedValue();

            GuiCycleButton preallocateButton = getCycleButton(4);
            boolean preallocate = preallocateButton.getSelectedValue();
            if(preallocate != config.preallocateMemory) restart = true;
            config.preallocateMemory = preallocate;

            GuiCycleButton boxRenderButton = getCycleButton(5);
            boolean boxRender = boxRenderButton.getSelectedValue();
            if(boxRender != config.fastBoxRender) restart = true;
            config.fastBoxRender = boxRender;

            GuiCycleButton fogButton = getCycleButton(6);
            boolean fog = fogButton.getSelectedValue();
            if(fog != config.fog) restart = true;
            config.fog = fog;

            GuiCycleButton hopperButton = getCycleButton(7);
            boolean fastHopper = hopperButton.getSelectedValue();
            if(fastHopper != config.fastHopper) restart = true;
            config.fastHopper = fastHopper;

            GuiCycleButton beaconButton = getCycleButton(8);
            boolean fastBeacon = beaconButton.getSelectedValue();
            if(fastBeacon != config.fastBeacon) restart = true;
            config.fastBeacon = fastBeacon;

            GuiCycleButton beaconRenderButton = getCycleButton(9);
            boolean fastBeaconRender = beaconRenderButton.getSelectedValue();
            if(fastBeaconRender != config.fastBeaconRender) restart = true;
            config.fastBeaconRender = fastBeaconRender;

            BetterFpsHelper.saveConfig();

            mc.displayGuiScreen(restart ? new GuiRestartDialog(parent) : parent);
        } else if(button.id == -2) {
            mc.displayGuiScreen(parent);
        }
    }

    private GuiCycleButton getCycleButton(int id) {
        for(GuiButton button : buttonList) {
            if(button.id == id) {
                return (GuiCycleButton)button;
            }
        }
        return null;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return true;
    }

    private static class AlgorithmButton extends GuiCycleButton {
        Process process = null;
        public <T> AlgorithmButton(int buttonId, String title, HashMap<T, String> values, T defaultValue, String[] helpLines) {
            super(buttonId, title, values, defaultValue, helpLines);
        }

        private String getJavaDir() {
            String separator = System.getProperty("file.separator");
            String path = System.getProperty("java.home") + separator + "bin" + separator;
            if((Util.getOSType() == EnumOS.WINDOWS) && (new File(path + "javaw.exe").isFile())) {
                return path + "javaw.exe";
            }
            return path + "java";
        }

        private boolean isRunning() {
            try {
                process.exitValue();
                return false;
            } catch(Exception ex) {
                return true;
            }
        }

        private void updateAlgorithm() {
            if((process != null) && (!isRunning())) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while((line = in.readLine()) != null) {
                        if(BetterFpsHelper.helpers.containsKey(line)) {
                            BetterFpsHelper.LOG.info("Found an algorithm! (" + line + ")");
                            for(int i = 0; i < keys.size(); i++) {
                                if(keys.get(i).equals(line)) {
                                    key = i;
                                    break;
                                }
                            }
                        }
                    }
                } catch(Exception ex) {}
                updateTitle();
                process = null;
            }
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            updateAlgorithm();
            super.drawButton(mc, mouseX, mouseY);
        }

        @Override
        public boolean shiftClick() {
            if((process != null) && (isRunning())) {
                return true;
            }

            BetterFpsHelper.LOG.info("Testing algorithms...");
            List<String> args = new ArrayList<String>();
            args.add(getJavaDir());
            args.add("-Dtester=" + Minecraft.getMinecraft().mcDataDir.getAbsolutePath());
            args.add("-cp");
            args.add(BetterFpsTweaker.class.getProtectionDomain().getCodeSource().getLocation().getFile());
            args.add("BetterFpsInstaller");

            try {
                process = new ProcessBuilder(args).start();
                displayString = "Testing...";
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            return true;
        }
    }

    private static class UpdateCheckerButton extends GuiBooleanButton {
        public UpdateCheckerButton(int buttonId, String title, boolean defaultValue, String[] helpLines) {
            super(buttonId, title, defaultValue, helpLines);
        }

        @Override
        public boolean shiftClick() {
            Minecraft.getMinecraft().thePlayer.addChatComponentMessage(new TextComponentString("Checking updates..."));
            UpdateChecker.checkForced();
            return true;
        }
    }
}
