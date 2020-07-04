import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Main{

    private static EventManager manager;
    public static void main(String[] args) {
        manager = new EventManager();
        JDABuilder builder = JDABuilder.createDefault("NzI3MTc2ODE2NjA3MTAxMDcw.XvoC0A.iTomhTLEbt2ai90KoUT8dpzaBYg");
        builder.setActivity(Activity.watching("Deep learning videos"));
        builder.addEventListeners(manager);
        JDA bot = null;
        try{
            bot = builder.build();
        } catch(LoginException e){
            System.out.println("Erreur de connexion");
        }
        try {
            bot.awaitReady();
            manager.setUser(bot.getSelfUser());
            System.out.println("Bot pret !");
        } catch (InterruptedException e) {
            //TODO: handle exception
        }

        System.out.println(bot.getGuilds());
    }
}