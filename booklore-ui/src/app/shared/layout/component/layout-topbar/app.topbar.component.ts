import {Component, ElementRef, OnDestroy, ViewChild} from '@angular/core';
import {MenuItem} from 'primeng/api';
import {LayoutService} from '../layout-main/service/app.layout.service';
import {Router, RouterLink} from '@angular/router';
import {DynamicDialogRef} from 'primeng/dynamicdialog';
import {TooltipModule} from 'primeng/tooltip';
import {FormsModule} from '@angular/forms';
import {InputTextModule} from 'primeng/inputtext';
import {BookSearcherComponent} from '../../../../features/book/components/book-searcher/book-searcher.component';
import {AsyncPipe, NgClass, NgStyle} from '@angular/common';
import {NotificationEventService} from '../../../websocket/notification-event.service';
import {Button} from 'primeng/button';
import {StyleClass} from 'primeng/styleclass';
import {Divider} from 'primeng/divider';
import {ThemeConfiguratorComponent} from '../theme-configurator/theme-configurator.component';
import {AuthService} from '../../../service/auth.service';
import {UserService} from '../../../../features/settings/user-management/user.service';
import {Popover} from 'primeng/popover';
import {takeUntil} from 'rxjs/operators';
import {Subject} from 'rxjs';
import {DialogLauncherService} from '../../../services/dialog-launcher.service';
import {UnifiedNotificationBoxComponent} from '../../../components/unified-notification-popover/unified-notification-popover-component';
import {Severity, LogNotification} from '../../../websocket/model/log-notification.model';
import {Menu} from 'primeng/menu';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {AVAILABLE_LANGS, LANG_LABELS} from '../../../../core/config/transloco-loader';
import {LANG_STORAGE_KEY} from '../../../../core/config/language-initializer';
import {SUPPORT_ANIMATION_KEY} from '../../../../features/settings/global-preferences/global-preferences.component';

@Component({
  selector: 'app-topbar',
  templateUrl: './app.topbar.component.html',
  styleUrls: ['./app.topbar.component.scss'],
  standalone: true,
  imports: [
    RouterLink,
    TooltipModule,
    FormsModule,
    InputTextModule,
    BookSearcherComponent,
    Button,
    ThemeConfiguratorComponent,
    StyleClass,
    NgClass,
    Divider,
    AsyncPipe,
    Popover,
    UnifiedNotificationBoxComponent,
    NgStyle,
    Menu,
    TranslocoDirective,
  ],
})
export class AppTopBarComponent implements OnDestroy {
  items!: MenuItem[];
  ref?: DynamicDialogRef;
  statsMenuItems: MenuItem[] = [];

  @ViewChild('menubutton') menuButton!: ElementRef;
  @ViewChild('topbarmenubutton') topbarMenuButton!: ElementRef;
  @ViewChild('topbarmenu') menu!: ElementRef;
  @ViewChild('statsMenu') statsMenu: Menu | undefined;

  isMenuVisible = true;
  showPulse = false;
  supportAnimationEnabled = localStorage.getItem(SUPPORT_ANIMATION_KEY) !== 'false';

  private eventTimer: number | undefined;
  private destroy$ = new Subject<void>();

  private latestNotificationSeverity?: Severity;

  activeLang = '';
  langMenuItems: MenuItem[] = [];

  private translocoService: TranslocoService;

  constructor(
    public layoutService: LayoutService,
    private notificationService: NotificationEventService,
    private router: Router,
    private authService: AuthService,
    protected userService: UserService,
    private dialogLauncher: DialogLauncherService,
    translocoService: TranslocoService
  ) {
    this.translocoService = translocoService;
    this.activeLang = translocoService.getActiveLang();
    this.langMenuItems = AVAILABLE_LANGS.map(lang => ({
      label: LANG_LABELS[lang] || lang,
      icon: lang === this.activeLang ? 'pi pi-check' : undefined,
      command: () => this.switchLanguage(lang),
    }));
    this.onStorageChange = this.onStorageChange.bind(this);
    window.addEventListener('storage', this.onStorageChange);

    this.subscribeToNotifications();

    this.userService.userState$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.initializeStatsMenu();
      });

    this.translocoService.langChanges$
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.initializeStatsMenu();
      });
  }

  ngOnDestroy(): void {
    if (this.ref) this.ref.close();
    clearTimeout(this.eventTimer);
    window.removeEventListener('storage', this.onStorageChange);
    this.destroy$.next();
    this.destroy$.complete();
  }

  private onStorageChange(event: StorageEvent): void {
    if (event.key === SUPPORT_ANIMATION_KEY) {
      this.supportAnimationEnabled = event.newValue !== 'false';
    }
  }

  toggleMenu() {
    this.isMenuVisible = !this.isMenuVisible;
    this.layoutService.onMenuToggle();
  }

  openGithubSupportDialog(): void {
    this.dialogLauncher.openGithubSupportDialog();
  }

  openLibraryCreatorDialog(): void {
    this.dialogLauncher.openLibraryCreateDialog();
  }

  openUserProfileDialog(): void {
    this.dialogLauncher.openUserProfileDialog();
  }

  navigateToSettings() {
    this.router.navigate(['/settings']);
  }

  navigateToMetadataManager() {
    this.router.navigate(['/metadata-manager']);
  }

  navigateToStats() {
    this.router.navigate(['/library-stats']);
  }

  navigateToUserStats() {
    this.router.navigate(['/reading-stats']);
  }

  switchLanguage(lang: string) {
    if (lang === this.activeLang) return;
    this.translocoService.load(lang).subscribe(() => {
      this.translocoService.setActiveLang(lang);
      localStorage.setItem(LANG_STORAGE_KEY, lang);
      this.activeLang = lang;
      this.langMenuItems = AVAILABLE_LANGS.map(l => ({
        label: LANG_LABELS[l] || l,
        icon: l === lang ? 'pi pi-check' : undefined,
        command: () => this.switchLanguage(l),
      }));
    });
  }

  logout() {
    this.authService.logout();
  }

  handleStatsButtonClick(event: Event) {
    if (this.statsMenuItems.length === 0) {
      return;
    }

    if (this.statsMenuItems.length === 1) {
      this.statsMenuItems[0].command?.({originalEvent: event, item: this.statsMenuItems[0]});
    }
  }

  private subscribeToNotifications() {
    this.notificationService.latestNotification$
      .pipe(takeUntil(this.destroy$))
      .subscribe((notification: LogNotification) => {
        this.latestNotificationSeverity = notification.severity;
        this.triggerPulseEffect();
      });
  }

  private triggerPulseEffect() {
    this.showPulse = true;
    clearTimeout(this.eventTimer);
    this.eventTimer = setTimeout(() => {
      this.showPulse = false;
    }, 4000) as unknown as number;
  }

  private initializeStatsMenu() {
    const userState = this.userService.userStateSubject.value;
    const user = userState.user;

    this.statsMenuItems = [];

    if (user?.permissions?.canAccessLibraryStats || user?.permissions?.admin) {
      this.statsMenuItems.push({
        label: this.translocoService.translate('layout.topbar.libraryStats'),
        icon: 'pi pi-chart-line',
        command: () => this.navigateToStats()
      });
    }

    if (user?.permissions?.canAccessUserStats || user?.permissions?.admin) {
      this.statsMenuItems.push({
        label: this.translocoService.translate('layout.topbar.readingStats'),
        icon: 'pi pi-users',
        command: () => this.navigateToUserStats()
      });
    }
  }

  get hasStatsAccess(): boolean {
    return this.statsMenuItems.length > 0;
  }

  get shouldShowStatsMenu(): boolean {
    return this.statsMenuItems.length > 1;
  }

  get statsTooltip(): string {
    if (this.statsMenuItems.length === 0) {
      return this.translocoService.translate('layout.topbar.stats');
    }
    if (this.statsMenuItems.length === 1) {
      return this.statsMenuItems[0].label || this.translocoService.translate('layout.topbar.stats');
    }
    return this.translocoService.translate('layout.topbar.stats');
  }

  get iconColor(): string {
    if (this.showPulse) {
      switch (this.latestNotificationSeverity) {
        case Severity.ERROR:
          return 'crimson';
        case Severity.INFO:
          return 'aqua';
        case Severity.WARN:
          return 'orange';
        default:
          return 'orange';
      }
    }
    return 'inherit';
  }

  get iconPulsating(): boolean {
    return this.showPulse;
  }
}
