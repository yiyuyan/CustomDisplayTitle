package cn.ksmcbrigade.ctd;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mod("cdt")
@Mod.EventBusSubscriber
public class CustomDisplayTitle{

    public static final Path path = Paths.get("config/cdt-config.txt");
    public static String MinecraftTitle = "Minecraft* 1.18.1";
    public static int Time = 60;

    public static boolean isStop;

    public static Thread TitleThread;

    public CustomDisplayTitle() {
        MinecraftForge.EVENT_BUS.register(this);
        InitMod();
    }

    @SubscribeEvent
    public static void RegisterCommand(RegisterCommandsEvent event){
        event.getDispatcher().register(Commands.literal("SetDisplayTitle").then(Commands.argument("title", StringArgumentType.string()).executes(args -> {
            try {
                String title = StringArgumentType.getString(args, "title");
                Files.write(path,(Time+";"+title).getBytes());
                ResetModConfig();
                setMinecraftTitle(title);
                Minecraft.getInstance().getWindow().setTitle(getMinecraftTitle());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return 0;
        })));

        event.getDispatcher().register(Commands.literal("SetWaitTimeBeforeSetDisplayEvent").then(Commands.argument("time", IntegerArgumentType.integer(1)).executes(args -> {
            try {
                Files.write(path,(IntegerArgumentType.getInteger(args,"time")+";"+getMinecraftTitle()).getBytes());
                ResetModConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return 0;
        })));

        event.getDispatcher().register(Commands.literal("ResetConfig").executes(args -> {
            ResetModConfig();
            return 0;
        }));

        event.getDispatcher().register(Commands.literal("RestartSetDisplayEvent").executes(args -> {
            isStop = true;
            TitleThread = new Thread(new TitleThread(),"TitleThread-"+String.valueOf(Math.round(Math.random()*999999)));
            isStop = false;
            TitleThread.start();
            return 0;
        }));
    }

    public static void ResetModConfig(){
        File ConfigFile = new File("config/cdt-config.txt");
        if(!(ConfigFile.exists())){
            try {
                Files.write(path,"10;Minecraft* 1.18.1".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            Time = Integer.parseInt(Files.readString(path).split(";")[0]);
            setMinecraftTitle(Files.readString(path).split(";")[1]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void InitMod(){
        ResetModConfig();
        TitleThread = new Thread(new TitleThread(),"TitleThread-"+String.valueOf(Math.round(Math.random()*999999)));
        TitleThread.start();
    }

    public static void setMinecraftTitle(String minecraftTitle) {
        MinecraftTitle = minecraftTitle;
    }

    public static String getMinecraftTitle(){
        return MinecraftTitle;
    }

    static class TitleThread implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(1000L * Time);
                System.out.println("Custom title thread is start!");
                while (!isStop){
                    Minecraft.getInstance().getWindow().setTitle(getMinecraftTitle());
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}