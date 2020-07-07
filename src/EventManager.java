import java.util.HashMap;
import java.util.List;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

// TODO - add custom emotes for 11 to 30 scores
// TODO - set the permissions
// TODO - save prefix and permissions
public class EventManager extends ListenerAdapter {

    private SelfUser bot;
    private HashMap<Guild, ServerSettings> guildsSets;

    public EventManager(){
        this.bot = null;
        this.guildsSets = null;
    }

    public void onGuildJoin(GuildJoinEvent event){
        this.guildsSets.put(event.getGuild(), new ServerSettings(event.getGuild()));
    }

    public void onMessageReceived(MessageReceivedEvent event){
        if(event.isFromGuild()){
            Message msg = event.getMessage();
            String msgTxt = msg.getContentRaw();
            if(!msg.getAuthor().isBot()){
                String[] args = msgTxt.split(" ");
                MessageChannel chann = event.getChannel();
                if(args[0].equals(this.guildsSets.get(event.getGuild()).getPrefix()+"moy")){
                    args = msgTxt.split("\"");
                    if(args.length == 3){
                        try {
                            // Used to test if the second arg is a number
                            int maxScore = Integer.parseInt(args[2].trim());

                            chann.sendMessage(args[1]).append("\nMoyenne : 0/"+maxScore)
                            .queue(message -> {if(maxScore <= 10){
                                                    for(int i=0; i < maxScore; i++){
                                                        message.addReaction("U+003"+String.valueOf(i)+" U+FE0F U+20E3").queue();
                                                    }
                                                    if(maxScore == 10){
                                                        message.addReaction("U+1F51F").queue();
                                                    } else{
                                                        message.addReaction("U+003"+maxScore+" U+FE0F U+20E3").queue();
                                                    }
                                                }});
                        } catch (NumberFormatException e) {
                            chann.sendMessage(args[1]).append("\nMoyenne : ?").queue();
                        }
                    } else{
                        chann.sendMessage("Commande incorrecte\n"+this.guildsSets.get(event.getGuild()).getPrefix()+"moy \"MESSAGE\" NOTE_MAX").queue();
                    }
                } else if(args[0].equals(this.guildsSets.get(event.getGuild()).getPrefix()+"prefix")){
                    if(args.length == 2 && args[1].length() == 1){
                        char oldPref = this.guildsSets.get(event.getGuild()).getPrefix();
                        char newPref = args[1].charAt(0);
                        this.guildsSets.get(event.getGuild()).setPrefix(newPref);
                        chann.sendMessage("Le préfixe des commandes a été changé de \""+oldPref+"\" à \""+newPref+"\"").queue();
                    } else{
                        chann.sendMessage("Commande incorrecte\n"+this.guildsSets.get(event.getGuild()).getPrefix()+"prefix CARACTERE").queue();
                    }
                } else if(args[0].equals(this.guildsSets.get(event.getGuild()).getPrefix()+"perms")){
                    this.guildsSets.get(event.getGuild()).refresh();
                    if(args.length == 1){
                        String listPerms = "";
                        String rName;
                        for(Role r : event.getGuild().getRoles()){
                            if(!r.isManaged()){
                                rName = r.getName();
                                if(rName.length() > 1 && rName.charAt(0) == '@'){
                                    rName = rName.substring(1);
                                }
                                listPerms = listPerms + "• "+rName+"\n";
                                if(this.guildsSets.get(event.getGuild()).getReactPermission(r) == Permissions.ALLOWED){
                                    listPerms = listPerms + "React : \u2705\n";
                                } else{
                                    listPerms = listPerms + "React : \u274C\n";
                                }
                                if(this.guildsSets.get(event.getGuild()).getPollPermission(r) == Permissions.ALLOWED){
                                    listPerms = listPerms + "Poll : \u2705\n";
                                } else{
                                    listPerms = listPerms + "Poll : \u274C\n";
                                }
                            }
                        }
                        chann.sendMessage(listPerms).queue();
                    } else if(args.length >= 4){
                        List<Role> mRoles = msg.getMentionedRoles();
                        try{
                            Permissions newPerm = Permissions.valueOf(args[args.length-1]);
                            // Set perms for everyone role if mentioned
                            if(msg.mentionsEveryone()){
                                List<Role> serverRoles = event.getGuild().getRoles();
                                boolean found = false;
                                int i = 0;
                                while(i < serverRoles.size() && !found){
                                    if(serverRoles.get(i).isPublicRole()){
                                        found = true;
                                        if(args[1].equals("poll")){
                                            this.guildsSets.get(event.getGuild()).setPollPermission(serverRoles.get(i), newPerm);
                                        } else if(args[1].equals("react")){
                                            this.guildsSets.get(event.getGuild()).setReactPermission(serverRoles.get(i), newPerm);
                                        }
                                    }
                                    i++;
                                }
                            }
                            if(args[1].equals("poll")){
                                for(Role r : mRoles){
                                    this.guildsSets.get(event.getGuild()).setPollPermission(r, newPerm);
                                }
                            } else if(args[1].equals("react")){
                                for(Role r : mRoles){
                                    this.guildsSets.get(event.getGuild()).setReactPermission(r, newPerm);
                                }
                            } else{
                                chann.sendMessage("Commande incorrecte\n"+this.guildsSets.get(event.getGuild()).getPrefix()+"perms [react|poll role [roles additionnels...] ALLOWED|DENIED]").queue();
                            }
                            chann.sendMessage("Les nouvelles permissions sont appliquées").queue();
                        } catch(IllegalArgumentException e){
                            chann.sendMessage("Commande incorrecte\n"+this.guildsSets.get(event.getGuild()).getPrefix()+"perms [react|poll role [roles additionnels...] ALLOWED|DENIED]").queue();
                        }
                    } else{
                        chann.sendMessage("Commande incorrecte\n"+this.guildsSets.get(event.getGuild()).getPrefix()+"perms [react|poll role [roles additionnels...] ALLOWED|DENIED]").queue();
                    }
                }
            }
        }
    }


    public void buildEmbedMoyMessage(String title, String text, String footer){
        
    }

    public void onMessageReactionAdd(MessageReactionAddEvent event){
        event.retrieveMessage().queue(message -> {
            if(message.getAuthor().equals(this.bot)){
                int maxScore = Integer.parseInt(message.getContentRaw().split("/")[1]);
                double sumScore = 0;
                int nbMarks = 0;
                double avg = 0;
                List<MessageReaction> l = message.getReactions();
                if(l.size() > maxScore){
                    for(int i=0; i <= maxScore; i++){
                        sumScore = sumScore + (i*(l.get(i).getCount()-1));
                        nbMarks = nbMarks + l.get(i).getCount()-1;
                    }
                    if(nbMarks > 0){
                        avg = sumScore/nbMarks;
                    }
                }
                String txt = message.getContentRaw().split("\n")[0];
                message.editMessage(txt+"\nMoyenne : "+avg+"/"+maxScore).queue();
                // TODO - use for perms   event.getReaction().removeReaction(event.getUser()).queue();
            }
        });
    }

    public void onMessageReactionRemove(MessageReactionRemoveEvent event){
        event.retrieveMessage().queue(message -> {
            if(message.getAuthor().equals(this.bot)){
                int maxScore = Integer.parseInt(message.getContentRaw().split("/")[1]);
                double sumScore = 0;
                int nbMarks = 0;
                double avg = 0;
                List<MessageReaction> l = message.getReactions();
                if(l.size() > maxScore){
                    for(int i=0; i <= maxScore; i++){
                        sumScore = sumScore + (i*(l.get(i).getCount()-1));
                        nbMarks = nbMarks + l.get(i).getCount()-1;
                    }
                    if(nbMarks > 0){
                        avg = sumScore/nbMarks;
                    }
                }
                String txt = message.getContentRaw().split("\n")[0];
                message.editMessage(txt+"\nMoyenne : "+avg+"/"+maxScore).queue();
            }
        });
    }

    public void setUser(SelfUser user){
        if(user != null){
            this.bot = user;
            this.guildsSets = new HashMap<Guild, ServerSettings>();
            for(Guild guild : this.bot.getJDA().getGuilds()){
                this.guildsSets.put(guild, new ServerSettings(guild));
            }
        }
    }
}