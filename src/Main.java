import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Main{

    private static EventManager manager;
    public static void main(String[] args) {
        manager = new EventManager();
        JDABuilder builder = JDABuilder.createDefault("");
        builder.setActivity(Activity.watching("Deep learning videos (-help)"));
        builder.addEventListeners(manager);
        try{
            JDA bot = builder.build();
            bot.awaitReady();
            manager.setUser(bot.getSelfUser());
        } catch(LoginException e){
            System.out.println("Erreur de connexion");
        } catch (InterruptedException e) {}
    }
}