package com.gab.notifoad.services;

import com.gab.notifoad.entity.Toad;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.queries.messages.MessagesGetLongPollHistoryQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service("manager")
public class Manager {
    
    @Value("${chatId}")
    Integer CHAT_ID;
    
    @Value("${appId}")
    private Integer APP_ID;
    
    @Value("${token}")
    private String TOKEN;
    
    @Value("${toadTitle}")
    private String TOAD_TITLE;
    
    private static final TransportClient transportClient = new HttpTransportClient();
    private static final VkApiClient vkApiClient = new VkApiClient(transportClient);
    private static final Random random = new Random();
    
    private GroupActor actor;
    
    private static List<String> whoPhrases = List.of("лучший на этом свете", "дурачок", "потный чел", "кент", "на свете всех милее", "царь людей, царь зверей");
    private static List<String> racePhrases = List.of("Ну что, народ, погнали нахуй!", "Давно я вас не видел в уличных гонках...", "Последний заезд (честно)...");
    private static List<String> eatPhrases = List.of("Кушать охота", "ЕДААААА", "Пора подкрепиться");
    private static List<String> defeatPhrases = List.of("...", "Да япона мать", "Етишкинъ сенокос", "Мать твою за ногу", "Нихуёво жмыхнуло", ":(", "Кпц");
    private static List<String> luckyPhrases = List.of("Вот это ништяк", "Повезло повезло...", "ОООООО", "ЕЕЕЕ бойййййй", ")0))))");
    
    private Toad toad;
    
    @PostConstruct
    private void after() {
        
        toad = new Toad();
        actor = new GroupActor(APP_ID, TOKEN);
    }
    
    @Scheduled(initialDelayString = "${defaultInitialDelay}", fixedRateString = "${workStartInterval}")
    public void workStart() {
    
        if(toad.isWork())
            return;
        
        if(random.nextDouble() < 0.05)
            executeCommand(Command.WORK_COOK);
        
        if(toad.getBugs() > 1500)
            executeCommand(Command.WORK_CROUPIER);
        else {
            
            double chanceThief = random.nextDouble();
            if(toad.getBugs() >= 350) {
                chanceThief += (toad.getSatiety()+5 >= toad.getTarget()) ? 0.2 : 0.1;
            }
            
            if(chanceThief > 0.5)
                executeCommand(Command.WORK_THIEF);
            else
                executeCommand(Command.WORK_CROUPIER);
        }
        
        toad.setWork(true);
    }
    @Scheduled(initialDelayString = "${workFinishDelay}", fixedRateString = "${workFinishInterval}")
    public void workFinish() {
        
        if(!toad.isWork())
            return;
        
        executeCommand(Command.WORK_FINISH);
        
        changeToadStats(getLastMessages(2));
        
        toad.setWork(false);
    }
    
    @Scheduled(cron = "${cronIntervalToadOfDay}")
    public void toadOfDay() {
        
        executeCommand(Command.TOAD_OF_THE_DAY);
        
        String answer = getLastMessages(1).get(0);
        if(answer.contains(TOAD_TITLE)) {
    
            changeToadStats(List.of(answer));
            sendPhrase(getRandomPhrase(luckyPhrases));
        }
        else
            sendPhrase(getRandomPhrase(defeatPhrases));
    }
    
    @Scheduled(fixedRateString = "${recruitInterval}")
    public void recruit() {
        
        if(!toad.isGangExists() && toad.getGangToads() < 11) {
            executeCommand(Command.RECRUIT);
            toad.recruit();
            if(toad.getGangToads() == 11)
                executeCommand(Command.GET_GANG);
        }
    }
    
    @Scheduled(initialDelayString = "${defaultInitialDelay}", fixedRateString = "${randomActionInterval}")
    public void randomAction() {
        
        double chance = random.nextDouble();
        
        if(chance < 0.25)
            executeCommand(Command.KISS);
        else if(chance < 0.5)
            executeCommand(Command.HUGS);
        else if(chance < 0.75)
            executeCommand(Command.BRAWL);
        else
            executeCommand(Command.WHO);
    }
    
    @Scheduled(cron = "${cronCheckInterval}")
    public void checkStats() {
    
        executeCommand(Command.CHECK_INFO);
        setStats(getLastMessages(1).get(0));
    }
    
    @Scheduled(initialDelayString = "${defaultInitialDelay}", fixedRateString = "${raceCheckInterval}")
    public void race() throws InterruptedException {
    
        
        String proposal = getLastMessages().stream()
                .filter(m -> m.contains("Гонка ")).findFirst().orElse("");
        if(proposal.isEmpty() || toad.isWork())
            return;
        
        try {
    
            int bet = Integer.parseInt(proposal.split(" ")[1]);
    
            if (toad.getBugs() > 150 && bet < toad.getBugs() * 0.5) {
                sendPhrase(getRandomPhrase(racePhrases));
                executeCommand(Command.RACE);
                executeCommand(Command.RACE_READY);
                executeCommand(Command.RACE_START);
                Thread.sleep(10000);
                changeToadStats(getLastMessages(2));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Scheduled(initialDelayString = "${defaultInitialDelay}", fixedRateString = "${eatInterval}")
    public void eat() {
    
        sendPhrase(getRandomPhrase(eatPhrases));
        
        if(toad.getBugs() >= 500) {
            executeCommand(Command.FATTEN);
            toad.fatten();
        }
        else {
            toad.feed();
            executeCommand(Command.FEED);
        }
    
        changeToadStats(getLastMessages(2));
    }
    
    @Scheduled(fixedRateString = "${marriageRewardInterval}")
    public void marriageReward() {
        
        if(toad.isMarried())
            executeCommand(Command.MARRIAGE_REWARD);
        
        
        changeToadStats(getLastMessages(2));
    }
    
    @Scheduled(initialDelayString = "${defaultInitialDelay}", fixedRateString = "${reviveCheckInterval}")
    public void revive() {
        
        if(!toad.isAlive()) {
            toad.heal();
            executeCommand(Command.REVIVE);
        }
    }
    
    
    @Scheduled(initialDelayString = "${defaultInitialDelay}", fixedRateString = "${partyCheckInterval}")
    public void party() throws InterruptedException {
        
        if(!toad.isParty() && !toad.isWork()) {
        String proposal = getLastMessages().stream()
                .filter(m -> m.equals("Жабу на тусу"))
                .findFirst().orElse("");
        
        if(!proposal.isEmpty()) {
            executeCommand(Command.PARTY);
            toad.setParty(true);
            executeCommand(Command.PARTY_START);
            Thread.sleep(10000);
            sendPhrase(getRandomPhrase(luckyPhrases));
            changeToadStats(getLastMessages());
        }
        }
    }
    
    private String getRandomPhrase(List<String> list) {
        
        return list.get(random.nextInt(list.size()));
    }
    
    private void executeCommand(Command command) {
    
        try {
            
            if(command.equals(Command.WHO))
                vkApiClient.messages().send(actor)
                        .message(String.format(command.getText(), getRandomPhrase(whoPhrases)))
                        .chatId(CHAT_ID).randomId(random.nextInt()).execute();
            else if(command.equals(Command.RACE))
                vkApiClient.messages().send(actor)
                        .message(String.format(command.getText(), random.nextInt(50, 100)))
                        .chatId(CHAT_ID).randomId(random.nextInt()).execute();
            else
                vkApiClient.messages().send(actor).message(command.getText())
                        .chatId(CHAT_ID).randomId(random.nextInt()).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }
    }
    
    private void setStats(String info) {
        
        try {
            String[] lines = info.split("\n");
            toad.setLevel(Integer.parseInt(lines[1].split("Уровень вашей жабы: ")[1]));
            toad.setSatiety(Integer.parseInt(lines[2].split("\\uD83C\\uDF70Сытость: ")[1].split("/")[0]));
            toad.setAlive(lines[4].contains("Живая"));
            toad.setBugs(Integer.parseInt(lines[5].split("\\uD83D\\uDC1EБукашки: ")[1]));
            toad.setHappy(Integer.parseInt(lines[7].split("\\(")[1].split("\\)")[0]));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void changeToadStats(List<String> answers) {
        
        try {
    
            for (String answer : answers) {
        
                int bugs = 0;
                if (answer.contains("🐞: +"))
                    bugs = Integer.parseInt(answer.split("🐞: +")[1].split(" ")[0]);
        
                int satiety = 0;
                if (answer.contains("\uD83C\uDF70: +"))
                    satiety = Integer.parseInt(answer.split("\uD83C\uDF70: +")[1].split(" ")[0]);
        
                toad.changeBugs(bugs);
                toad.addSatiety(satiety);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void sendPhrase(String phrase) {
    
        try {
            vkApiClient.messages().send(actor).message(phrase)
                    .chatId(CHAT_ID).randomId(random.nextInt()).execute();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }
    }
    
    private List<String> getLastMessages(int count) {
    
        List<String> textMessages = Collections.emptyList();
        try {
            Integer ts = vkApiClient.messages().getLongPollServer(actor).execute().getTs();
            MessagesGetLongPollHistoryQuery historyQuery = vkApiClient.messages().getLongPollHistory(actor).ts(ts);
            textMessages = historyQuery.execute().getMessages().getItems().stream()
                    .sorted(Comparator.comparing(Message::getDate).reversed())
                    .limit(count)
                    .map(Message::getText).collect(Collectors.toList());
//            textMessages = vkApiClient.messages().getHistory(actor).userId(CHAT_ID).count(count).execute()
//                    .getItems().stream().map(Message::getText).collect(Collectors.toList());
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }
    
        return textMessages;
    }
    
    private List<String> getLastMessages() {
        
        List<String> textMessages = Collections.emptyList();
        try {
            Integer ts = vkApiClient.messages().getLongPollServer(actor).execute().getTs();
            MessagesGetLongPollHistoryQuery historyQuery = vkApiClient.messages().getLongPollHistory(actor).ts(ts);
            textMessages = historyQuery.execute().getMessages().getItems().stream()
                    .sorted(Comparator.comparing(Message::getDate).reversed())
                    .map(Message::getText).collect(Collectors.toList());
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }
        
        return textMessages;
    }
}

enum Command {
    
    RECRUIT("Взять жабу"),
    FEED("Покормить жабу"),
    MARRIAGE_REWARD("Брак вознаграждение"),
    FATTEN("Откормить жабу"),
    TOAD_OF_THE_DAY("Жаба дня"),
    CLAN_REWARD("Клан вознаграждение"),
    BRONZE_DUNGEON("Отправить жабу в бронзовое подземелье"),
    SILVER_DUNGEON("Отправить жабу в серебряное подземелье"),
    GOLD_DUNGEON("Отправить жабу в золотое подземелье"),
    WORK_CROUPIER("Работа крупье"),
    WORK_THIEF("Работа грабитель"),
    WORK_COOK("Поход в столовую"),
    WORK_FINISH("Завершить работу"),
    CHECK_INFO("Моя жаба"),
    REVIVE("Реанимировать жабу"),
    CANDY("Использовать леденцы"),
    HUGS("Обнять всех"),
    BRAWL("Отмудохать всех"),
    KISS("Поцеловать всех"),
    WHO("Жабабот кто %s"),
    RACE("Гонка %d"),
    RACE_READY("Статус готов"),
    RACE_START("Гонка старт"),
    GET_GANG("Собрать банду"),
    PARTY("Жабу на тусу"),
    PARTY_START("Начать тусу");
    
    private String text;
    
    Command(String t) {
        
        text = t;
    }
    
    public String getText() {
        return text;
    }
}
