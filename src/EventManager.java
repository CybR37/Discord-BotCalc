import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventManager extends ListenerAdapter {

    private SelfUser bot;

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        Message msg = event.getMessage();
        String msgTxt = msg.getContentRaw();
        if(!msg.getAuthor().isBot()){
            String[] args = msgTxt.split(" ");
            if(args[0].equals("-moy")){
                args = msgTxt.split("\"");
                MessageChannel chann = event.getChannel();
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
                    chann.sendMessage("Commande incorrecte\n-moy \"MESSAGE\" NOTE_MAX").queue();
                }
            }
        }
    }


    public void buildEmbedMoyMessage(String text, int maxScore){
        
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
        }
    }
}