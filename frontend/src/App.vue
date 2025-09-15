<template>
  <div id="app" class="min-h-screen flex">

    <!-- Sidebar -->
    <aside
        class="hidden md:flex md:flex-col transition-all duration-200 ease-in-out overflow-hidden"
        :class="ui.sidebarCollapsed ? 'w-0 px-0 py-0' : 'w-72 p-4'">

      <!-- Brand -->
      <div v-show="!ui.sidebarCollapsed" class="card p-4">
        <div class="flex items-center gap-3">
          <img :src="branding.logoUrl" :style="{height: branding.logoH + 'px'}"
               class="w-auto" alt="Gridscale" @error="onLogoError"/>
          <div class="min-w-0">
            <div class="font-semibold tracking-wide">Gridscale CMS</div>
            <div class="text-xs text-gray-500">Bringing new value to the future</div>
          </div>
        </div>
      </div>

      <!-- Nav -->
      <nav v-show="!ui.sidebarCollapsed" class="card p-2 mt-3">
        <button v-for="item in nav" :key="item.key"
                @click="route=item.key"
                class="w-full text-left px-3 py-2 rounded-xl flex items-center gap-3 hover:bg-brand-50"
                :class="route===item.key ? 'bg-brand-50 text-brand-700 ring-1 ring-brand-100' : ''">
          <i :class="item.icon" class="text-brand-500"></i>
          <span class="font-medium">{{ item.label }}</span>
          <span v-if="item.badge!=null"
                class="ml-auto text-[11px] px-2 py-0.5 rounded-full bg-gray-100 text-gray-700 border border-gray-200">
            {{ item.badge() }}
          </span>
        </button>
      </nav>

      <!-- Admin / User Registration -->
      <div v-show="!ui.sidebarCollapsed" class="card p-4 mt-3">
        <div class="flex items-center justify-between">
          <h3 class="font-semibold flex items-center gap-2">
            <i class="fa-solid fa-shield-halved text-brand-600"></i> 管理者
          </h3>
          <span v-if="security.adminEnabled"
                class="text-xs px-2 py-0.5 rounded-full"
                :class="adminUnlocked ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-800'">
            {{ adminUnlocked ? '解除済み' : 'ロック中' }}
          </span>
        </div>

        <!-- Unlock -->
        <div v-if="!adminUnlocked" class="mt-2">
          <label class="block text-sm">管理者パスコード</label>
          <input v-model="adminPassInput" type="password" class="mt-1 w-full input" placeholder="••••••••">
          <button @click="unlockAdmin" class="mt-2 w-full px-3 py-2 rounded-lg btn-primary">
            ロック解除
          </button>
          <p class="text-xs text-gray-500 mt-2" v-if="!security.adminEnabled">
            ※パスコード未設定（デモモード）。だれでも登録可能です。<br>「設定 → セキュリティ」でパスコードを有効化してください。
          </p>
        </div>

        <!-- User Create (Admin only) -->
        <UserRegistrationComponent
          v-if="isAuthenticated && currentUser?.userType === 'admin'"
          :users="users"
          :security="security"
          :admin-unlocked="adminUnlocked"
          @user-created="handleUserCreated"
          @admin-relocked="handleAdminRelocked"
          @password-generated="handlePasswordGenerated"
        />
      </div>

      <!-- Data Export/Import -->
      <div v-show="!ui.sidebarCollapsed" class="mt-auto card p-3 text-sm">
        <div class="flex items-center justify-between">
          <span class="text-gray-600">データ</span>
          <div class="space-x-2">
            <button @click="exportJSON" class="px-2 py-1 rounded border">JSON</button>
            <button @click="exportCSV" class="px-2 py-1 rounded border">CSV</button>
          </div>
        </div>
        <label class="mt-2 block">
          <input type="file" class="hidden" @change="importFile($event)" accept="application/json">
          <span class="inline-flex items-center gap-2 px-2 py-1 mt-1 rounded border cursor-pointer">
            <i class="fa-solid fa-file-import"></i> インポート
          </span>
        </label>
      </div>
    </aside>

    <!-- Main -->
    <section class="flex-1 flex flex-col">
      <!-- Topbar -->
      <header class="px-4 pt-4 md:px-6">
        <div class="card px-3 py-2 md:px-4 md:py-3 flex items-center gap-3">
          <!-- Mobile drawer -->
          <button class="md:hidden px-2 py-1 rounded border" @click="drawer=!drawer" title="メニュー">
            <i class="fa-solid fa-bars"></i>
          </button>
          <!-- Desktop sidebar collapse toggle -->
          <button
              class="hidden md:inline-flex px-2 py-1 rounded border hover:bg-gray-50"
              @click="toggleSidebar"
              :aria-pressed="ui.sidebarCollapsed ? 'true':'false'"
              :title="ui.sidebarCollapsed ? 'サイドバーを表示' : 'サイドバーを隠す'">
            <i :class="ui.sidebarCollapsed ? 'fa-solid fa-angles-right' : 'fa-solid fa-angles-left'"></i>
          </button>

          <img :src="branding.logoUrl" :style="{height: Math.min(branding.logoH, 28) + 'px'}"
               class="hidden sm:block w-auto" alt="Gridscale" @error="onLogoError"/>

          <h1 class="text-base md:text-lg font-semibold flex items-center gap-2">
            <i :class="current.icon" class="text-brand-600"></i> {{ current.label }}
          </h1>

          <div class="ml-auto flex items-center gap-3 text-xs md:text-sm text-gray-600">
            <span>下書き {{ stats.drafts }} / 公開 {{ stats.published }}</span>

            <!-- Authentication Section -->
            <div v-if="isAuthenticated" class="flex items-center gap-2">
              <span class="text-brand-700">{{ currentUser?.userName || currentUser?.email }}</span>
              <button @click="handleLogout" class="px-3 py-1 rounded border text-red-600 hover:bg-red-50">
                ログアウト
              </button>
            </div>
            <button v-else @click="showLoginModal = true" class="px-3 py-1 rounded border btn-primary">
              ログイン
            </button>
          </div>
        </div>
      </header>

      <!-- Drawer (mobile) -->
      <transition name="fade">
        <div v-if="drawer" class="md:hidden px-4">
          <div class="card mt-3 p-2 grid grid-cols-2 gap-2">
            <button v-for="item in nav" :key="item.key" @click="route=item.key; drawer=false"
                    class="px-3 py-2 rounded-xl border text-left flex items-center gap-2 hover:bg-brand-50">
              <i :class="item.icon" class="text-brand-600"></i><span>{{ item.label }}</span>
            </button>
          </div>
        </div>
      </transition>

      <!-- ===== Content (News / Whitepapers / Contact / Recruit / Settings) ===== -->
      <main class="max-w-7xl mx-auto w-full px-4 md:px-6 py-6 space-y-6">
        <!-- NEWS -->
        <section v-if="route==='news'" class="space-y-6">
          <NewsListComponent
            ref="newsListComponent"
            @create-news="openNewsForm()"
            @edit-news="openNewsForm"
            @delete-news="delNews"
          />

          <!-- Editor -->
          <transition name="fade">
            <div v-if="modals.news" class="card p-4">
              <h3 class="font-semibold mb-3">{{ newsForm.id ? 'ニュースを編集' : 'ニュースを作成' }}</h3>
              <div class="grid md:grid-cols-2 gap-4">
                <label class="block">タイトル
                  <input v-model="newsForm.title" class="mt-1 w-full input"/>
                </label>
                <label class="block">日付
                  <input v-model="newsForm.date" type="date" class="mt-1 w-full input"/>
                </label>
                <label class="block md:col-span-2">本文
                  <textarea v-model="newsForm.body" rows="6" class="mt-1 w-full input"></textarea>
                </label>
                <div class="md:col-span-2 flex items-center justify-end gap-2">
                  <button @click="modals.news=false" class="px-3 py-2 rounded border">キャンセル</button>
                  <button @click="saveNews" class="px-3 py-2 rounded-lg btn-primary">保存</button>
                </div>
              </div>
            </div>
          </transition>
        </section>

        <!-- WHITEPAPERS -->
        <section v-if="route==='whitepapers'" class="space-y-6">
          <div class="card p-4">
            <div class="flex items-center justify-between">
              <h2 class="font-semibold">ホワイトペーパー</h2>
              <div class="flex items-center gap-2">
                <input v-model="filters.wp.q" placeholder="検索：タイトル/タグ" class="input w-56">
                <button @click="openWPForm()" class="px-3 py-2 rounded-lg btn-primary">
                  <i class="fa-solid fa-plus mr-1"></i> 新規登録
                </button>
              </div>
            </div>

            <div class="grid md:grid-cols-3 gap-4 mt-4">
              <article v-for="w in filteredWP" :key="w.id" class="card p-4">
                <div class="flex items-center justify-between mb-2">
                  <h3 class="font-semibold">{{ w.title }}</h3>
                  <span class="text-xs px-2 py-0.5 rounded-full"
                        :class="w.published?'bg-brand-100 text-brand-800':'bg-gray-100 text-gray-700'">
                    {{ w.published ? '公開' : '下書き' }}
                  </span>
                </div>
                <p class="text-sm text-gray-600 line-clamp-3">{{ w.desc }}</p>
                <div class="mt-3 flex flex-wrap gap-2">
                  <span v-for="t in (w.tags||[])" :key="t" class="tag">{{ t }}</span>
                </div>
                <div class="mt-4 flex items-center justify-between text-sm">
                  <a v-if="w.url" :href="w.url" target="_blank" class="text-brand-700 hover:underline">
                    <i class="fa-solid fa-file-arrow-down mr-1"></i> プレビュー/ダウンロード
                  </a>
                  <span class="text-gray-500 ml-auto">{{ w.date }}</span>
                </div>
                <div class="mt-3 flex gap-2">
                  <button @click="openWPForm(w)" class="px-2 py-1 rounded border hover:bg-gray-50">
                    <i class="fa-solid fa-pen"></i>
                  </button>
                  <button @click="delWP(w.id)" class="px-2 py-1 rounded border text-red-600 hover:bg-red-50">
                    <i class="fa-solid fa-trash"></i>
                  </button>
                </div>
              </article>
            </div>
            <p v-if="filteredWP.length===0" class="text-center text-gray-500 py-10">登録がありません</p>
          </div>

          <!-- Editor -->
          <transition name="fade">
            <div v-if="modals.wp" class="card p-4">
              <h3 class="font-semibold mb-3">{{ wpForm.id ? 'ホワイトペーパーを編集' : 'ホワイトペーパーを登録' }}</h3>
              <div class="grid md:grid-cols-2 gap-4">
                <label class="block">タイトル
                  <input v-model="wpForm.title" class="mt-1 w-full input"/>
                </label>
                <label class="block">公開日
                  <input v-model="wpForm.date" type="date" class="mt-1 w-full input"/>
                </label>
                <label class="block md:col-span-2">説明
                  <textarea v-model="wpForm.desc" rows="4" class="mt-1 w-full input"></textarea>
                </label>
                <label class="block">公開URL（PDF等）
                  <input v-model="wpForm.url" placeholder="https://example.com/whitepaper.pdf" class="mt-1 w-full input"/>
                </label>
                <label class="block">タグ（, 区切り）
                  <input v-model="wpTags" placeholder="DX, AI, 金融" class="mt-1 w-full input"/>
                </label>
                <label class="inline-flex items-center gap-2">
                  <input type="checkbox" v-model="wpForm.published"><span>公開する</span>
                </label>
                <div class="md:col-span-2 flex items-center justify-end gap-2">
                  <button @click="modals.wp=false" class="px-3 py-2 rounded border">キャンセル</button>
                  <button @click="saveWP" class="px-3 py-2 rounded-lg btn-primary">保存</button>
                </div>
              </div>
            </div>
          </transition>
        </section>

        <!-- CONTACT -->
        <section v-if="route==='contact'" class="space-y-6">
          <div class="card p-4">
            <div class="flex items-center justify-between">
              <h2 class="font-semibold">お問い合わせ</h2>
              <div class="flex items-center gap-2">
                <select v-model="filters.contact.status" class="input">
                  <option value="">すべて</option>
                  <option value="open">未対応</option>
                  <option value="replied">返信済み</option>
                </select>
                <input v-model="filters.contact.q" placeholder="検索：名前/件名/本文" class="input w-56">
              </div>
            </div>

            <div class="mt-4 overflow-auto">
              <table class="min-w-full text-sm">
                <thead class="border-b bg-gray-50">
                <tr>
                  <th class="text-left p-2">日時</th>
                  <th class="text-left p-2">氏名</th>
                  <th class="text-left p-2">件名</th>
                  <th class="text-left p-2">内容</th>
                  <th class="text-left p-2">状態</th>
                  <th class="text-right p-2">操作</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="i in filteredContacts" :key="i.id" class="border-b align-top">
                  <td class="p-2 whitespace-nowrap">{{ i.createdAt }}</td>
                  <td class="p-2">{{ i.name }}<div class="text-xs text-gray-500">{{ i.email }}</div></td>
                  <td class="p-2">{{ i.subject || '-' }}</td>
                  <td class="p-2 max-w-[420px]">
                    <div class="text-gray-700 whitespace-pre-wrap line-clamp-3">{{ i.message }}</div>
                  </td>
                  <td class="p-2">
                    <span class="text-xs px-2 py-0.5 rounded-full"
                          :class="i.status==='replied'?'bg-emerald-100 text-emerald-800':'bg-amber-100 text-amber-800'">
                      {{ i.status==='replied'?'返信済み':'未対応' }}
                    </span>
                  </td>
                  <td class="p-2 text-right space-x-2 whitespace-nowrap">
                    <button @click="openContact(i)" class="px-2 py-1 rounded border hover:bg-gray-50">
                      <i class="fa-solid fa-eye"></i>
                    </button>
                    <button @click="delContact(i.id)" class="px-2 py-1 rounded border text-red-600 hover:bg-red-50">
                      <i class="fa-solid fa-trash"></i>
                    </button>
                  </td>
                </tr>
                <tr v-if="filteredContacts.length===0">
                  <td colspan="6" class="p-6 text-center text-gray-500">該当なし</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Detail & Reply -->
          <transition name="fade">
            <div v-if="modals.contact" class="card p-4">
              <div class="flex items-start justify-between gap-6">
                <div class="flex-1">
                  <h3 class="font-semibold mb-1">{{ contactForm.subject || '（件名なし）' }}</h3>
                  <div class="text-sm text-gray-500 mb-4">{{ contactForm.name }} ・ {{ contactForm.email }} ・ {{ contactForm.createdAt }}</div>
                  <p class="whitespace-pre-wrap text-gray-800">{{ contactForm.message }}</p>
                  <div v-if="(contactForm.replies||[]).length" class="mt-6">
                    <h4 class="font-semibold text-sm mb-2">返信履歴</h4>
                    <div class="space-y-2">
                      <div v-for="r in contactForm.replies" :key="r.at" class="p-3 rounded-lg bg-gray-50 border">
                        <div class="text-xs text-gray-500 mb-1">{{ r.at }}</div>
                        <div class="whitespace-pre-wrap text-sm text-gray-800">{{ r.body }}</div>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="w-full max-w-md">
                  <label class="block text-sm font-medium">返信内容</label>
                  <textarea v-model="replyBody" rows="8" class="mt-1 w-full input"></textarea>
                  <div class="mt-3 flex items-center justify-end gap-2">
                    <button @click="modals.contact=false" class="px-3 py-2 rounded border">閉じる</button>
                    <button :disabled="!replyBody.trim()" @click="sendReply"
                            class="px-3 py-2 rounded-lg btn-primary disabled:opacity-50">
                      <i class="fa-solid fa-paper-plane mr-1"></i> 返信を記録
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </transition>
        </section>

        <!-- RECRUIT -->
        <section v-if="route==='recruit'" class="space-y-6">
          <div class="card p-4">
            <div class="flex items-center justify-between">
              <h2 class="font-semibold">採用エントリー</h2>
              <div class="flex items-center gap-2">
                <select v-model="filters.rec.status" class="input">
                  <option value="">すべて</option>
                  <option value="new">新規</option>
                  <option value="screening">書類審査</option>
                  <option value="interview">面談</option>
                  <option value="offer">内定</option>
                  <option value="rejected">不合格</option>
                </select>
                <input v-model="filters.rec.q" placeholder="検索：名前/スキル/メモ" class="input w-56">
                <button @click="openRecForm()" class="px-3 py-2 rounded-lg btn-primary">
                  <i class="fa-solid fa-user-plus mr-1"></i> 手動登録
                </button>
              </div>
            </div>

            <div class="mt-4 overflow-auto">
              <table class="min-w-full text-sm">
                <thead class="border-b bg-gray-50">
                <tr>
                  <th class="text-left p-2">応募日</th>
                  <th class="text-left p-2">氏名</th>
                  <th class="text-left p-2">連絡先</th>
                  <th class="text-left p-2">スキル</th>
                  <th class="text-left p-2">状態</th>
                  <th class="text-right p-2">操作</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="c in filteredRec" :key="c.id" class="border-b">
                  <td class="p-2 whitespace-nowrap">{{ c.appliedAt }}</td>
                  <td class="p-2 font-medium">{{ c.name }}</td>
                  <td class="p-2">
                    <div class="text-xs text-gray-500">{{ c.email }}</div>
                    <div class="text-xs text-gray-500">{{ c.tel || '-' }}</div>
                  </td>
                  <td class="p-2 text-gray-700">{{ c.skills || '-' }}</td>
                  <td class="p-2">
                    <select v-model="c.stage" @change="persist()" class="px-2 py-1 rounded border bg-white">
                      <option value="new">新規</option>
                      <option value="screening">書類審査</option>
                      <option value="interview">面談</option>
                      <option value="offer">内定</option>
                      <option value="rejected">不合格</option>
                    </select>
                  </td>
                  <td class="p-2 text-right space-x-2 whitespace-nowrap">
                    <button @click="openRecForm(c)" class="px-2 py-1 rounded border hover:bg-gray-50">
                      <i class="fa-solid fa-pen"></i>
                    </button>
                    <button @click="delRec(c.id)" class="px-2 py-1 rounded border text-red-600 hover:bg-red-50">
                      <i class="fa-solid fa-trash"></i>
                    </button>
                  </td>
                </tr>
                <tr v-if="filteredRec.length===0">
                  <td colspan="6" class="p-6 text-center text-gray-500">該当なし</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Editor -->
          <transition name="fade">
            <div v-if="modals.rec" class="card p-4">
              <h3 class="font-semibold mb-3">{{ recForm.id ? '応募者を編集' : '応募者を登録' }}</h3>
              <div class="grid md:grid-cols-2 gap-4">
                <label class="block">氏名
                  <input v-model="recForm.name" class="mt-1 w-full input"/>
                </label>
                <label class="block">応募日
                  <input v-model="recForm.appliedAt" type="date" class="mt-1 w-full input"/>
                </label>
                <label class="block">メール
                  <input v-model="recForm.email" type="email" class="mt-1 w-full input"/>
                </label>
                <label class="block">電話
                  <input v-model="recForm.tel" class="mt-1 w-full input"/>
                </label>
                <label class="block md:col-span-2">スキル
                  <input v-model="recForm.skills" placeholder="Java / Vue / AWS 等" class="mt-1 w-full input"/>
                </label>
                <label class="block md:col-span-2">メモ
                  <textarea v-model="recForm.note" rows="4" class="mt-1 w-full input"></textarea>
                </label>
                <div class="md:col-span-2 flex items-center justify-end gap-2">
                  <button @click="modals.rec=false" class="px-3 py-2 rounded border">キャンセル</button>
                  <button @click="saveRec" class="px-3 py-2 rounded-lg btn-primary">保存</button>
                </div>
              </div>
            </div>
          </transition>
        </section>

        <!-- USERS -->
        <section v-if="route==='users'" class="space-y-6">
          <div class="card p-4">
            <div class="flex items-center justify-between">
              <h2 class="font-semibold">ユーザー管理</h2>
              <div class="flex items-center gap-2">
                <select v-model="filters.users.role" class="input">
                  <option value="">全ての権限</option>
                  <option value="admin">管理者</option>
                  <option value="editor">編集者</option>
                  <option value="viewer">閲覧のみ</option>
                </select>
                <select v-model="filters.users.status" class="input">
                  <option value="">全ての状態</option>
                  <option value="active">有効</option>
                  <option value="inactive">無効</option>
                </select>
                <input v-model="filters.users.q" placeholder="検索：名前/メール" class="input w-56">
              </div>
            </div>

            <div class="mt-4 overflow-auto">
              <table class="min-w-full text-sm">
                <thead class="border-b bg-gray-50">
                <tr>
                  <th class="text-left p-2">名前</th>
                  <th class="text-left p-2">メール</th>
                  <th class="text-left p-2">権限</th>
                  <th class="text-left p-2">状態</th>
                  <th class="text-left p-2">作成日</th>
                  <th class="text-right p-2">操作</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="u in filteredUsers" :key="u.id || u.userId" class="border-b">
                  <td class="p-2 font-medium">{{ u.name || u.userName }}</td>
                  <td class="p-2 text-gray-600">{{ u.email }}</td>
                  <td class="p-2">
                    <span class="px-2 py-1 rounded-full text-xs"
                          :class="(u.role || u.userType) === 'admin' ? 'bg-red-100 text-red-700' :
                                  (u.role || u.userType) === 'editor' ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-700'">
                      {{ roleLabel(u.role || u.userType) }}
                    </span>
                  </td>
                  <td class="p-2">
                    <span class="px-2 py-1 rounded-full text-xs"
                          :class="(u.active !== undefined ? u.active : u.activeFlag) ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-700'">
                      {{ (u.active !== undefined ? u.active : u.activeFlag) ? '有効' : '無効' }}
                    </span>
                  </td>
                  <td class="p-2 text-gray-500 whitespace-nowrap">
                    {{ (u.createdAt || '').replace('T', ' ').slice(0, 16) }}
                  </td>
                  <td class="p-2 text-right space-x-2 whitespace-nowrap">
                    <button @click="openUserForm(u)" class="px-2 py-1 rounded border hover:bg-gray-50">
                      <i class="fa-solid fa-pen"></i>
                    </button>
                  </td>
                </tr>
                <tr v-if="filteredUsers.length===0">
                  <td colspan="6" class="p-6 text-center text-gray-500">該当なし</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- User Edit Modal -->
          <transition name="fade">
            <div v-if="modals.user" class="card p-4">
              <h3 class="font-semibold mb-3">ユーザー情報を編集</h3>
              <div class="grid md:grid-cols-2 gap-4">
                <label class="block">名前
                  <input v-model="userForm.userName" class="mt-1 w-full input"/>
                </label>
                <label class="block">メール
                  <input v-model="userForm.email" type="email" class="mt-1 w-full input"/>
                </label>
                <label class="block">権限
                  <select v-model="userForm.userType" class="mt-1 w-full input">
                    <option value="admin">管理者</option>
                    <option value="editor">編集者</option>
                    <option value="viewer">閲覧のみ</option>
                  </select>
                </label>
                <label class="block">状態
                  <select v-model="userForm.activeFlag" class="mt-1 w-full input">
                    <option :value="true">有効</option>
                    <option :value="false">無効</option>
                  </select>
                </label>
                <div class="md:col-span-2 flex items-center justify-end gap-2">
                  <button @click="modals.user=false" class="px-3 py-2 rounded border">キャンセル</button>
                  <button @click="saveUser" class="px-3 py-2 rounded-lg btn-primary">保存</button>
                </div>
              </div>
            </div>
          </transition>
        </section>

        <!-- SETTINGS -->
        <section v-if="route==='settings'" class="space-y-6">
          <!-- Publish -->
          <div class="card p-4">
            <h2 class="font-semibold mb-4">公開・連携設定（ダミー）</h2>
            <div class="grid md:grid-cols-2 gap-6">
              <div>
                <h3 class="font-semibold mb-2">News公開先</h3>
                <label class="flex items-center gap-2 mb-2"><input type="checkbox" v-model="settings.publish.home"> TOPページ（/index.htmlのNEWS）</label>
                <label class="flex items-center gap-2 mb-2"><input type="checkbox" v-model="settings.publish.api"> APIエンドポイント（/api/news）</label>
                <p class="text-xs text-gray-500">※フロント側では保存のみ。サーバ側実装時に参照してください。</p>
              </div>
              <div>
                <h3 class="font-semibold mb-2">通知</h3>
                <label class="flex items-center gap-2 mb-2"><input type="checkbox" v-model="settings.notify.mail"> 新規問い合わせのメール通知</label>
                <label class="flex items-center gap-2"><input type="checkbox" v-model="settings.notify.slack"> Slack通知</label>
              </div>
            </div>
          </div>

          <!-- Branding -->
          <div class="card p-4">
            <h2 class="font-semibold mb-4">ブランド設定（ロゴ）</h2>
            <div class="grid md:grid-cols-2 gap-6 items-start">
              <div>
                <label class="block text-sm font-medium">ロゴURL</label>
                <input v-model="branding.logoUrl" class="mt-1 w-full input" placeholder="/img/logo.png">
                <label class="block text-sm font-medium mt-4">ロゴ画像をアップロード</label>
                <input type="file" accept="image/*" class="mt-1 w-full" @change="uploadLogo($event)">
                <label class="block text-sm font-medium mt-4">ロゴ高さ（px）</label>
                <input type="range" min="20" max="64" v-model.number="branding.logoH" class="w-full">
                <div class="mt-2 text-sm text-gray-600">{{ branding.logoH }} px</div>
                <div class="mt-4 flex gap-2">
                  <button @click="saveBranding" class="px-3 py-2 rounded-lg btn-primary">保存</button>
                  <button @click="resetBranding" class="px-3 py-2 rounded border">初期化</button>
                </div>
              </div>
              <div>
                <div class="border rounded-xl p-4 bg-gray-50">
                  <div class="text-sm text-gray-600 mb-2">プレビュー</div>
                  <div class="flex items-center gap-3">
                    <img :src="branding.logoUrl" :style="{height: branding.logoH + 'px'}" class="w-auto" alt="logo preview" @error="onLogoError">
                    <span class="text-gray-700 font-medium">Gridscale CMS</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Security -->
          <div class="card p-4">
            <h2 class="font-semibold mb-4">セキュリティ（管理者パスコード）</h2>
            <label class="inline-flex items-center gap-2">
              <input type="checkbox" v-model="security.adminEnabled">
              <span>管理者パスコードを有効化（ユーザー登録は管理者のみ）</span>
            </label>
            <div class="grid md:grid-cols-2 gap-4 mt-4">
              <label class="block">新しいパスコード
                <input v-model="security.newPass" type="password" class="mt-1 w-full input" placeholder="8文字以上">
              </label>
              <label class="block">確認用
                <input v-model="security.newPass2" type="password" class="mt-1 w-full input" placeholder="もう一度">
              </label>
            </div>
            <div class="mt-3 flex items-center gap-2">
              <button @click="saveAdminPass" class="px-3 py-2 rounded-lg btn-primary">保存</button>
              <button @click="clearAdminPass" class="px-3 py-2 rounded border">無効化</button>
            </div>
            <p class="text-xs text-gray-500 mt-2">
              ※ パスコードはブラウザにソルト付きハッシュで保存されます（デモ用途）。本番はサーバ側（Spring Security 等）で実装してください。
            </p>
          </div>
        </section>
      </main>
    </section>

    <!-- Login Modal -->
    <LoginComponent
      :show-login="showLoginModal"
      @close="showLoginModal = false"
      @login-success="handleLoginSuccess"
    />
  </div>
</template>

<script>
import axios from 'axios'
import NewsListComponent from './components/NewsListComponent.vue'
import UserRegistrationComponent from './components/UserRegistrationComponent.vue'
import LoginComponent from './components/LoginComponent.vue'

const STORAGE_KEY = 'gridscale_cms_v1_light_logo_users_collapse'

export default {
  name: 'App',
  components: {
    NewsListComponent,
    UserRegistrationComponent,
    LoginComponent
  },
  data () {
    return {
      route: 'news',
      drawer: false,
      // UI状態（サイドバー折りたたみ）
      ui: { sidebarCollapsed: false },

      modals: { news: false, wp: false, contact: false, rec: false, user: false },
      filters: { news: { q: '' }, wp: { q: '' }, contact: { q: '', status: '' }, rec: { q: '', status: '' }, users: { q: '', role: '', status: '' } },
      news: [],
      whitepapers: [],
      contacts: [],
      recruits: [],
      users: [],
      newsForm: { id: null, title: '', date: '', body: '', published: true },
      wpForm: { id: null, title: '', date: '', desc: '', url: '', tags: [], published: false },
      wpTags: '',
      contactForm: {},
      replyBody: '',
      recForm: { id: null, name: '', appliedAt: '', email: '', tel: '', skills: '', note: '', stage: 'new' },
      userForm: { id: null, userName: '', email: '', userType: '', activeFlag: true },
      settings: { publish: { home: true, api: false }, notify: { mail: true, slack: false } },
      branding: { logoUrl: '/img/logo.png', logoH: 28 },
      security: { adminEnabled: false, adminSalt: '', adminHash: '', newPass: '', newPass2: '' },
      adminUnlocked: false,
      adminPassInput: '',
      // Authentication state
      isAuthenticated: false,
      currentUser: null,
      showLoginModal: false
    }
  },
  computed: {
    nav () {
      const baseNav = [
        { key: 'news', label: 'News', icon: 'fa-solid fa-bullhorn', badge: () => this.news.filter(n => n.published).length },
        { key: 'whitepapers', label: 'WhitePaper', icon: 'fa-solid fa-file-lines', badge: () => this.whitepapers.filter(w => w.published).length },
        { key: 'contact', label: 'お問い合わせ', icon: 'fa-solid fa-inbox', badge: () => this.contacts.filter(c => c.status === 'open').length },
        { key: 'recruit', label: '採用', icon: 'fa-solid fa-users', badge: () => this.recruits.length }
      ]

      // Add Users menu only for admin users
      if (this.isAuthenticated && this.currentUser?.userType === 'admin') {
        baseNav.push({ key: 'users', label: 'ユーザー', icon: 'fa-solid fa-user-group', badge: () => this.users.filter(u => u.active).length })
      }

      baseNav.push({ key: 'settings', label: '設定', icon: 'fa-solid fa-gear', badge: () => null })

      return baseNav
    },
    current () { return this.nav.find(n => n.key === this.route) || this.nav[0] },
    stats () {
      return {
        drafts: this.news.filter(n => !n.published).length + this.whitepapers.filter(w => !w.published).length,
        published: this.news.filter(n => n.published).length + this.whitepapers.filter(w => w.published).length
      }
    },
    filteredNews () {
      const q = this.filters.news.q.trim().toLowerCase()
      return this.news.filter(n => !q || (n.title + n.body).toLowerCase().includes(q))
        .sort((a, b) => (b.date || '').localeCompare(a.date || ''))
    },
    filteredWP () {
      const q = this.filters.wp.q.trim().toLowerCase()
      return this.whitepapers.filter(w => {
        const text = (w.title + ' ' + (w.tags || []).join(' ') + ' ' + (w.desc || '')).toLowerCase()
        return !q || text.includes(q)
      }).sort((a, b) => (b.date || '').localeCompare(a.date || ''))
    },
    filteredContacts () {
      const q = this.filters.contact.q.trim().toLowerCase(); const st = this.filters.contact.status
      return this.contacts.filter(i => {
        const hit = !q || (i.name + i.subject + i.message).toLowerCase().includes(q)
        const ok = !st || i.status === st; return hit && ok
      }).sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || '')).reverse()
    },
    filteredRec () {
      const q = this.filters.rec.q.trim().toLowerCase(); const st = this.filters.rec.status
      return this.recruits.filter(r => {
        const hit = !q || (r.name + ' ' + (r.skills || '') + ' ' + (r.note || '')).toLowerCase().includes(q)
        const ok = !st || r.stage === st; return hit && ok
      }).sort((a, b) => (b.appliedAt || '').localeCompare(a.appliedAt || ''))
    },
    recentUsers () {
      return [...this.users].sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || '')).slice(0, 5)
    },
    filteredUsers () {
      const q = this.filters.users.q.trim().toLowerCase()
      const role = this.filters.users.role
      const status = this.filters.users.status

      return this.users.filter(u => {
        const nameMatch = !q || (u.name || u.userName || '').toLowerCase().includes(q) || (u.email || '').toLowerCase().includes(q)
        const roleMatch = !role || u.role === role || u.userType === role
        const statusMatch = !status || (status === 'active' && u.active) || (status === 'inactive' && !u.active) ||
                           (status === 'active' && u.activeFlag) || (status === 'inactive' && !u.activeFlag)
        return nameMatch && roleMatch && statusMatch
      }).sort((a, b) => (b.createdAt || '').localeCompare(a.createdAt || ''))
    }
  },
  watch: {
    route (newRoute) {
      if (newRoute === 'users' && this.isAuthenticated && this.currentUser?.userType === 'admin') {
        this.loadUsers()
      }
    }
  },
  created () {
    const saved = JSON.parse(localStorage.getItem(STORAGE_KEY) || '{}')
    Object.assign(this.$data, { ...this.$data, ...saved })

    // 初回サンプル
    if (!saved.news) {
      this.news = [
        { id: crypto.randomUUID(), title: 'ホームページをリニューアルしました', date: new Date().toISOString().slice(0, 10), body: '静的から動的配信へ。今後も定期的に更新します。', published: true },
        { id: crypto.randomUUID(), title: '夏季休業のお知らせ', date: '2025-08-10', body: '8/13〜8/16は夏季休業です。', published: true }
      ]
    }
    if (!saved.whitepapers) {
      this.whitepapers = [
        { id: crypto.randomUUID(), title: 'DX推進ガイド（製造業編）', date: '2025-07-15', desc: '現場起点のカイゼンから始めるDXの実践手順。', url: '', tags: ['DX', '製造'], published: false }
      ]
    }
    if (!saved.contacts) {
      this.contacts = [
        { id: crypto.randomUUID(), name: '山田 太郎', email: 'taro@example.com', subject: 'SESのご相談', message: '9月から参画可能なJavaエンジニアのご紹介をお願いしたいです。', createdAt: '2025-09-10 10:24', status: 'open', replies: [] },
        { id: crypto.randomUUID(), name: 'Suzuki', email: 'suzuki@corp.jp', subject: 'Webサイトの制作', message: 'LP制作の概算見積をご相談できますか？', createdAt: '2025-09-09 16:02', status: 'replied', replies: [{ at: '2025-09-09 17:10', body: 'お問い合わせありがとうございます。詳細要件をお伺いできますか？' }] }
      ]
    }
    if (!saved.recruits) {
      this.recruits = [
        { id: crypto.randomUUID(), name: '佐藤 花子', email: 'hanako@example.jp', tel: '', skills: 'Java / Spring / Vue', appliedAt: '2025-09-12', note: 'バックエンド強め', stage: 'screening' }
      ]
    }
    if (!saved.users) {
      this.users = [
        { id: crypto.randomUUID(), name: 'Admin', email: 'admin@example.com', role: 'admin', active: true, note: 'デモ管理者', createdAt: new Date().toISOString(), auth: { salt: '', hash: '' } }
      ]
    }

    // セッションのロック状態
    this.adminUnlocked = sessionStorage.getItem('cms_admin_unlocked') === '1' || !this.security.adminEnabled

    // Initialize authentication
    this.initializeAuth()

    this.persist()
  },
  methods: {
    // === UI ===
    toggleSidebar () {
      this.ui.sidebarCollapsed = !this.ui.sidebarCollapsed
      this.persist()
    },

    // === Persistence & Utils ===
    persist () {
      const payload = {
        route: this.route,
        ui: this.ui,
        news: this.news,
        whitepapers: this.whitepapers,
        contacts: this.contacts,
        recruits: this.recruits,
        users: this.users,
        settings: this.settings,
        branding: this.branding,
        security: {
          adminEnabled: this.security.adminEnabled, adminSalt: this.security.adminSalt, adminHash: this.security.adminHash
        }
      }
      localStorage.setItem(STORAGE_KEY, JSON.stringify(payload))
    },
    clearAll () {
      if (confirm('全データをlocalStorageから削除します。よろしいですか？')) {
        localStorage.removeItem(STORAGE_KEY); sessionStorage.removeItem('cms_admin_unlocked'); location.reload()
      }
    },
    async hashHex (strOrBytes) {
      const data = (strOrBytes instanceof Uint8Array) ? strOrBytes : new TextEncoder().encode(String(strOrBytes))
      const buf = await crypto.subtle.digest('SHA-256', data)
      return Array.from(new Uint8Array(buf)).map(b => b.toString(16).padStart(2, '0')).join('')
    },
    randomBytes (n) { const a = new Uint8Array(n); crypto.getRandomValues(a); return a },
    async hashPassword (pass, saltHex) {
      const salt = saltHex ? Uint8Array.from(saltHex.match(/.{1,2}/g).map(h => parseInt(h, 16))) : this.randomBytes(16)
      const concat = new Uint8Array(salt.length + new TextEncoder().encode(pass).length)
      concat.set(salt, 0); concat.set(new TextEncoder().encode(pass), salt.length)
      const hash = await this.hashHex(concat)
      const saltOut = Array.from(salt).map(b => b.toString(16).padStart(2, '0')).join('')
      return { salt: saltOut, hash }
    },

    // === Admin Lock ===
    async unlockAdmin () {
      if (!this.security.adminEnabled) {
        this.adminUnlocked = true; sessionStorage.setItem('cms_admin_unlocked', '1'); return
      }
      const pass = this.adminPassInput || ''
      if (pass.length < 1) { alert('パスコードを入力してください'); return }
      const { hash } = await this.hashPassword(pass, this.security.adminSalt)
      if (hash === this.security.adminHash) {
        this.adminUnlocked = true; sessionStorage.setItem('cms_admin_unlocked', '1'); this.adminPassInput = ''
      } else {
        alert('パスコードが違います')
      }
    },

    // === Authentication Methods ===
    initializeAuth () {
      try {
        const authData = localStorage.getItem('auth')
        if (authData) {
          const parsedAuth = JSON.parse(authData)

          // Check if token is still valid (not expired)
          const loginTime = new Date(parsedAuth.loginTime)
          const now = new Date()
          const hoursSinceLogin = (now - loginTime) / (1000 * 60 * 60)

          // Token expires after 24 hours (as set in backend)
          if (hoursSinceLogin < 24) {
            this.currentUser = parsedAuth
            this.isAuthenticated = true

            // Set authorization header for all requests
            axios.defaults.headers.common.Authorization = `Bearer ${parsedAuth.token}`
          } else {
            // Token expired, clear stored data
            localStorage.removeItem('auth')
            this.handleLogout()
          }
        }
      } catch (error) {
        console.error('Failed to initialize auth:', error)
        localStorage.removeItem('auth')
      }
    },

    handleLoginSuccess (authData) {
      this.currentUser = authData
      this.isAuthenticated = true
      this.showLoginModal = false

      // Set authorization header for all requests
      axios.defaults.headers.common.Authorization = `Bearer ${authData.token}`

      console.log('User logged in:', authData)
    },

    handleLogout () {
      this.currentUser = null
      this.isAuthenticated = false
      this.showLoginModal = false

      // Clear stored authentication data
      localStorage.removeItem('auth')

      // Remove authorization header
      delete axios.defaults.headers.common.Authorization

      console.log('User logged out')
    },

    // === User Registration Event Handlers ===
    handleUserCreated (user) {
      this.users.push(user)
      this.persist()
    },
    handleAdminRelocked () {
      this.adminUnlocked = false
      sessionStorage.removeItem('cms_admin_unlocked')
    },
    handlePasswordGenerated (password) {
      // Optional: handle password generation event if needed
      console.log('Password generated:', password)
    },

    // === News / WP / Contact / Recruit ===
    openNewsForm (n = null) {
      if (n) {
        // When editing existing news, map content field to body field for the form
        this.newsForm = {
          id: n.id,
          title: n.title,
          date: n.date,
          body: n.content || '', // Map backend 'content' to frontend 'body'
          published: n.published !== undefined ? n.published : true
        }
      } else {
        // When creating new news
        this.newsForm = { id: null, title: '', date: new Date().toISOString().slice(0, 10), body: '', published: true }
      }
      this.modals.news = true
    },
    async saveNews () {
      const f = this.newsForm
      if (!f.title?.trim()) return alert('タイトルは必須です')
      if (!f.date) return alert('日付は必須です')

      try {
        const newsData = {
          title: f.title.trim(),
          date: f.date,
          content: f.body || ''
        }

        if (f.id) {
          // Edit existing news
          await axios.put(`/api/news/${f.id}`, newsData)
          alert('ニュースを更新しました')
        } else {
          // Create new news
          await axios.post('/api/news', newsData)
          alert('ニュースを作成しました')
        }

        // Refresh the news list component
        const newsListComponent = this.$refs.newsListComponent
        if (newsListComponent && newsListComponent.refreshNews) {
          await newsListComponent.refreshNews()
        }

        this.modals.news = false
      } catch (error) {
        console.error('Failed to save news:', error)
        const action = f.id ? '更新' : '作成'
        alert(`ニュースの${action}に失敗しました`)
      }
    },
    async delNews (id) {
      if (confirm('削除しますか？')) {
        try {
          await axios.delete(`/api/news/${id}`)

          // Refresh the news list component to reflect the deletion
          const newsListComponent = this.$refs.newsListComponent
          if (newsListComponent && newsListComponent.refreshNews) {
            await newsListComponent.refreshNews()
          }

          // Also remove from local data if it exists there
          this.news = this.news.filter(n => n.id !== id)
          this.persist()

          alert('ニュースを削除しました')
        } catch (error) {
          console.error('Failed to delete news:', error)
          alert('ニュースの削除に失敗しました')
        }
      }
    },

    openWPForm (w = null) {
      this.wpForm = w
        ? JSON.parse(JSON.stringify(w))
        : { id: null, title: '', date: new Date().toISOString().slice(0, 10), desc: '', url: '', tags: [], published: false }
      this.wpTags = (this.wpForm.tags || []).join(', ')
      this.modals.wp = true
    },
    saveWP () {
      const f = this.wpForm
      f.tags = this.wpTags.split(',').map(s => s.trim()).filter(Boolean)
      if (!f.title?.trim()) return alert('タイトルは必須です')
      if (f.id) {
        const idx = this.whitepapers.findIndex(x => x.id === f.id)
        if (idx > -1) this.whitepapers[idx] = JSON.parse(JSON.stringify(f))
      } else {
        f.id = crypto.randomUUID(); this.whitepapers.push(JSON.parse(JSON.stringify(f)))
      }
      this.modals.wp = false; this.persist()
    },
    delWP (id) {
      if (confirm('削除しますか？')) {
        this.whitepapers = this.whitepapers.filter(w => w.id !== id); this.persist()
      }
    },

    openContact (i) {
      this.contactForm = JSON.parse(JSON.stringify(i))
      this.replyBody = ''; this.modals.contact = true
    },
    sendReply () {
      const body = this.replyBody.trim(); if (!body) return
      const idx = this.contacts.findIndex(x => x.id === this.contactForm.id)
      if (idx > -1) {
        this.contacts[idx].replies = this.contacts[idx].replies || []
        this.contacts[idx].replies.push({ at: new Date().toISOString().slice(0, 16).replace('T', ' '), body })
        this.contacts[idx].status = 'replied'
        this.persist(); this.openContact(this.contacts[idx]); this.replyBody = ''
        alert('返信を記録しました（送信はダミー）')
      }
    },
    delContact (id) {
      if (confirm('削除しますか？')) {
        this.contacts = this.contacts.filter(n => n.id !== id); this.persist()
      }
    },

    openRecForm (c = null) {
      this.recForm = c
        ? JSON.parse(JSON.stringify(c))
        : { id: null, name: '', appliedAt: new Date().toISOString().slice(0, 10), email: '', tel: '', skills: '', note: '', stage: 'new' }
      this.modals.rec = true
    },
    saveRec () {
      const f = this.recForm
      if (!f.name?.trim()) return alert('氏名は必須です')
      if (!f.appliedAt) return alert('応募日は必須です')
      if (f.id) {
        const idx = this.recruits.findIndex(x => x.id === f.id)
        if (idx > -1) this.recruits[idx] = JSON.parse(JSON.stringify(f))
      } else {
        f.id = crypto.randomUUID(); this.recruits.push(JSON.parse(JSON.stringify(f)))
      }
      this.modals.rec = false; this.persist()
    },
    delRec (id) {
      if (confirm('削除しますか？')) {
        this.recruits = this.recruits.filter(n => n.id !== id); this.persist()
      }
    },

    // === User Management ===
    roleLabel (r) {
      return r === 'admin' ? '管理者' : (r === 'editor' ? '編集者' : '閲覧のみ')
    },
    async loadUsers () {
      if (!this.isAuthenticated || this.currentUser?.userType !== 'admin') {
        return
      }
      try {
        const response = await axios.get('/api/users')
        this.users = response.data.map(user => ({
          id: user.userId,
          userId: user.userId,
          name: user.userName,
          userName: user.userName,
          email: user.email,
          role: user.userType,
          userType: user.userType,
          active: user.activeFlag,
          activeFlag: user.activeFlag,
          createdAt: user.createdAt || new Date().toISOString()
        }))
        this.persist()
      } catch (error) {
        console.error('Failed to load users:', error)
        alert('ユーザー一覧の取得に失敗しました')
      }
    },
    openUserForm (u) {
      this.userForm = {
        id: u.id || u.userId,
        userName: u.name || u.userName,
        email: u.email,
        userType: u.role || u.userType,
        activeFlag: u.active !== undefined ? u.active : u.activeFlag
      }
      this.modals.user = true
    },
    async saveUser () {
      const f = this.userForm
      if (!f.userName?.trim()) return alert('名前は必須です')
      if (!f.email?.trim()) return alert('メールは必須です')
      if (!f.userType) return alert('権限は必須です')

      try {
        const userData = {
          userName: f.userName.trim(),
          email: f.email.trim(),
          userType: f.userType,
          activeFlag: f.activeFlag
        }

        await axios.put(`/api/users/${f.id}`, userData)

        // Update local data
        const idx = this.users.findIndex(u => (u.id || u.userId) === f.id)
        if (idx > -1) {
          this.users[idx] = {
            ...this.users[idx],
            name: f.userName,
            userName: f.userName,
            email: f.email,
            role: f.userType,
            userType: f.userType,
            active: f.activeFlag,
            activeFlag: f.activeFlag
          }
        }

        this.modals.user = false
        this.persist()
        alert('ユーザー情報を更新しました')
      } catch (error) {
        console.error('Failed to update user:', error)
        if (error.response && error.response.status === 409) {
          alert('このメールアドレスは既に使用されています')
        } else if (error.response && error.response.status === 403) {
          alert('権限がありません')
        } else {
          alert('ユーザー情報の更新に失敗しました')
        }
      }
    },

    // Security methods
    async saveAdminPass () {
      if (!this.security.newPass || this.security.newPass.length < 8) {
        alert('パスコードは8文字以上で入力してください'); return
      }
      if (this.security.newPass !== this.security.newPass2) {
        alert('パスコードが一致しません'); return
      }
      const { salt, hash } = await this.hashPassword(this.security.newPass)
      this.security.adminSalt = salt
      this.security.adminHash = hash
      this.security.adminEnabled = true
      this.security.newPass = ''
      this.security.newPass2 = ''
      this.persist()
      alert('管理者パスコードを設定しました')
    },
    clearAdminPass () {
      if (confirm('管理者パスコードを無効化しますか？')) {
        this.security.adminEnabled = false
        this.security.adminSalt = ''
        this.security.adminHash = ''
        this.adminUnlocked = true
        sessionStorage.setItem('cms_admin_unlocked', '1')
        this.persist()
        alert('管理者パスコードを無効化しました')
      }
    },

    // import/export
    exportJSON () {
      const blob = new Blob([localStorage.getItem(STORAGE_KEY) || '{}'], { type: 'application/json' })
      const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = 'gridscale_cms.json'; a.click(); URL.revokeObjectURL(a.href)
    },
    exportCSV () {
      const rows = [['type', 'id', 'date', 'title', 'body', 'published']]
      this.news.forEach(n => rows.push(['news', n.id, n.date, this.escapeCSV(n.title), this.escapeCSV(n.body), n.published]))
      const csv = rows.map(r => r.join(',')).join('\n')
      const blob = new Blob([csv], { type: 'text/csv' }); const a = document.createElement('a'); a.href = URL.createObjectURL(blob); a.download = 'gridscale_news.csv'; a.click(); URL.revokeObjectURL(a.href)
    },
    importFile (e) {
      const file = e.target.files[0]; if (!file) return
      const reader = new FileReader()
      reader.onload = () => {
        try {
          const obj = JSON.parse(reader.result)
          if (confirm('現在のデータを上書きしますか？')) {
            localStorage.setItem(STORAGE_KEY, JSON.stringify(obj))
            sessionStorage.removeItem('cms_admin_unlocked')
            location.reload()
          }
        } catch { alert('JSONの読み込みに失敗しました') }
      }
      reader.readAsText(file, 'utf-8')
    },
    escapeCSV (s) { s = (s ?? '') + ''; return (s.includes('"') || s.includes(',') || s.includes('\n')) ? `"${s.replaceAll('"', '""')}"` : s },

    // Branding / Logo
    uploadLogo (e) {
      const f = e.target.files?.[0]; if (!f) return
      const reader = new FileReader()
      reader.onload = () => { this.branding.logoUrl = reader.result; this.persist() }
      reader.readAsDataURL(f)
    },
    saveBranding () { this.persist(); alert('ブランド設定を保存しました') },
    resetBranding () {
      this.branding = { logoUrl: '/img/logo.png', logoH: 28 }
      this.persist()
    },
    onLogoError (ev) {
      ev.target.onerror = null
      ev.target.src = 'data:image/svg+xml;utf8,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" width="160" height="40"><rect width="160" height="40" fill="#eef2ff"/><text x="8" y="26" font-size="16" fill="#4f46e5">Logo not found</text></svg>')
    }
  }
}
</script>

<style>
body {
  background:
    radial-gradient(900px 600px at -10% -10%, rgba(99, 102, 241, .12), transparent 60%),
    radial-gradient(900px 600px at 110% 0%, rgba(14, 165, 233, .12), transparent 60%),
    linear-gradient(180deg, #f9fafb 0%, #ffffff 60%, #f8fafc 100%);
}

.fade-enter-active, .fade-leave-active { transition: opacity .18s ease }

.fade-enter-from, .fade-leave-to { opacity: 0 }

.card { background: #fff; border: 1px solid #e5e7eb; border-radius: 16px; box-shadow: 0 10px 25px -15px rgba(2, 6, 23, .15) }

.input { background: #fff; border: 1px solid #e5e7eb; border-radius: 10px; padding: .6rem .75rem }

.input:focus { outline: none; box-shadow: 0 0 0 3px rgba(99, 102, 241, .18) }

.btn-primary { background-image: linear-gradient(90deg, #6366f1, #06b6d4); color: #fff }

.btn-primary:hover { filter: brightness(1.05) }

.tag { background: #f3f4f6; border: 1px solid #e5e7eb; border-radius: 9999px; padding: .2rem .5rem; font-size: .75rem }
</style>
