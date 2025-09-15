package com.gridscale.nextpush.config;

import com.gridscale.nextpush.entity.Contact;
import com.gridscale.nextpush.entity.Entry;
import com.gridscale.nextpush.repository.ContactRepository;
import com.gridscale.nextpush.repository.EntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private EntryRepository entryRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if contacts already exist to avoid duplicating data
        if (contactRepository.count() == 0) {
            insertDummyContacts();
        }

        // Check if entries already exist to avoid duplicating data
        if (entryRepository.count() == 0) {
            insertDummyEntries();
        }
    }

    private void insertDummyContacts() {
        // Create dummy contact data
        Contact contact1 = new Contact();
        contact1.setName("山田 太郎");
        contact1.setEmail("yamada.taro@example.com");
        contact1.setSubject("SESのご相談");
        contact1.setMessage("9月から参画可能なJavaエンジニアのご紹介をお願いしたいです。経験年数は5年程度で、Spring BootとVue.jsの開発経験があります。詳細な要件をお聞かせいただければと思います。");
        contact1.setIpAddress("192.168.1.100");
        contact1.setCreatedAt(Instant.now().minus(5, ChronoUnit.DAYS));

        Contact contact2 = new Contact();
        contact2.setName("鈴木 花子");
        contact2.setEmail("suzuki.hanako@corp.jp");
        contact2.setSubject("Webサイトの制作依頼");
        contact2.setMessage("弊社のコーポレートサイトのリニューアルを検討しております。LP制作の概算見積をご相談できますか？レスポンシブデザインで、CMSも導入したいと考えています。");
        contact2.setIpAddress("10.0.0.50");
        contact2.setCreatedAt(Instant.now().minus(3, ChronoUnit.DAYS));

        Contact contact3 = new Contact();
        contact3.setName("田中 健司");
        contact3.setEmail("tanaka.kenji@tech.co.jp");
        contact3.setSubject("システム開発のお見積り");
        contact3.setMessage("在庫管理システムの開発をお願いしたいのですが、概算でどの程度の工数になりますでしょうか。既存のデータベースとの連携も必要です。一度お打ち合わせの時間をいただけますか？");
        contact3.setIpAddress("172.16.0.25");
        contact3.setCreatedAt(Instant.now().minus(2, ChronoUnit.DAYS));

        Contact contact4 = new Contact();
        contact4.setName("佐藤 美咲");
        contact4.setEmail("sato.misaki@startup.jp");
        contact4.setSubject("モバイルアプリ開発について");
        contact4.setMessage("スタートアップでモバイルアプリの開発を予定しています。iOS/Android両対応で、バックエンドAPIの開発も含めてお願いできますでしょうか。予算感についてもご相談したいです。");
        contact4.setIpAddress("203.0.113.42");
        contact4.setCreatedAt(Instant.now().minus(1, ChronoUnit.DAYS));

        Contact contact5 = new Contact();
        contact5.setName("高橋 雄一");
        contact5.setEmail("takahashi.yuichi@manufacturing.com");
        contact5.setSubject("DX推進に関するご相談");
        contact5.setMessage("製造業でのDX推進を検討しています。現場の業務改善から始めて、段階的にシステム化を進めたいと考えております。貴社のDX推進ガイドを拝見しましたが、詳細についてお聞かせください。");
        contact5.setIpAddress("198.51.100.15");
        contact5.setCreatedAt(Instant.now().minus(6, ChronoUnit.HOURS));

        // Save all contacts
        contactRepository.save(contact1);
        contactRepository.save(contact2);
        contactRepository.save(contact3);
        contactRepository.save(contact4);
        contactRepository.save(contact5);

        System.out.println("Dummy contact data inserted successfully!");
    }

    private void insertDummyEntries() {
        // Create dummy entry data
        Entry entry1 = new Entry();
        entry1.setName("田中 花子");
        entry1.setNameKana("タナカ ハナコ");
        entry1.setEmail("tanaka.hanako@university.ac.jp");
        entry1.setTel("090-1234-5678");
        entry1.setBirthYear("2001");
        entry1.setBirthMonth("4");
        entry1.setBirthDay("15");
        entry1.setGender("female");
        entry1.setGraduationYear("2025");
        entry1.setGraduationMonth("3");
        entry1.setAddress("東京都新宿区西新宿1-1-1");
        entry1.setPostcode("160-0023");
        entry1.setResumeFilename("tanaka_hanako_resume.pdf");
        entry1.setIpAddress("192.168.1.101");
        entry1.setCreatedAt(Instant.now().minus(3, ChronoUnit.DAYS));

        Entry entry2 = new Entry();
        entry2.setName("佐藤 太郎");
        entry2.setNameKana("サトウ タロウ");
        entry2.setEmail("sato.taro@tech-college.edu");
        entry2.setTel("080-9876-5432");
        entry2.setBirthYear("2000");
        entry2.setBirthMonth("8");
        entry2.setBirthDay("22");
        entry2.setGender("male");
        entry2.setGraduationYear("2024");
        entry2.setGraduationMonth("9");
        entry2.setAddress("大阪府大阪市北区梅田3-4-5");
        entry2.setPostcode("530-0001");
        entry2.setResumeFilename("sato_taro_resume.pdf");
        entry2.setIpAddress("10.0.0.25");
        entry2.setCreatedAt(Instant.now().minus(5, ChronoUnit.DAYS));

        Entry entry3 = new Entry();
        entry3.setName("山田 美咲");
        entry3.setNameKana("ヤマダ ミサキ");
        entry3.setEmail("yamada.misaki@student.jp");
        entry3.setTel("070-5555-7777");
        entry3.setBirthYear("2002");
        entry3.setBirthMonth("12");
        entry3.setBirthDay("3");
        entry3.setGender("female");
        entry3.setGraduationYear("2025");
        entry3.setGraduationMonth("3");
        entry3.setAddress("神奈川県横浜市港北区新横浜2-3-4");
        entry3.setPostcode("222-0033");
        entry3.setResumeFilename("yamada_misaki_resume.pdf");
        entry3.setIpAddress("172.16.0.30");
        entry3.setCreatedAt(Instant.now().minus(2, ChronoUnit.DAYS));

        Entry entry4 = new Entry();
        entry4.setName("鈴木 健一");
        entry4.setNameKana("スズキ ケンイチ");
        entry4.setEmail("suzuki.kenichi@engineering.ac.jp");
        entry4.setTel("090-8888-9999");
        entry4.setBirthYear("1999");
        entry4.setBirthMonth("6");
        entry4.setBirthDay("10");
        entry4.setGender("male");
        entry4.setGraduationYear("2024");
        entry4.setGraduationMonth("3");
        entry4.setAddress("愛知県名古屋市中区栄1-2-3");
        entry4.setPostcode("460-0008");
        entry4.setResumeFilename("suzuki_kenichi_resume.pdf");
        entry4.setIpAddress("203.0.113.50");
        entry4.setCreatedAt(Instant.now().minus(1, ChronoUnit.DAYS));

        Entry entry5 = new Entry();
        entry5.setName("高橋 あゆみ");
        entry5.setNameKana("タカハシ アユミ");
        entry5.setEmail("takahashi.ayumi@info-tech.edu");
        entry5.setTel("080-1111-2222");
        entry5.setBirthYear("2001");
        entry5.setBirthMonth("10");
        entry5.setBirthDay("28");
        entry5.setGender("female");
        entry5.setGraduationYear("2025");
        entry5.setGraduationMonth("3");
        entry5.setAddress("福岡県福岡市博多区博多駅前4-5-6");
        entry5.setPostcode("812-0011");
        entry5.setResumeFilename("takahashi_ayumi_resume.pdf");
        entry5.setIpAddress("198.51.100.75");
        entry5.setCreatedAt(Instant.now().minus(4, ChronoUnit.HOURS));

        // Save all entries
        entryRepository.save(entry1);
        entryRepository.save(entry2);
        entryRepository.save(entry3);
        entryRepository.save(entry4);
        entryRepository.save(entry5);

        System.out.println("Dummy entry data inserted successfully!");
    }
}