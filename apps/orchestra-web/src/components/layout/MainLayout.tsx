import React from 'react';
import { Outlet, NavLink, useLocation } from 'react-router-dom';
import { Music, FileText, Box, Database, Moon, Sun, LayoutGrid, Settings, Sparkles, History } from 'lucide-react';
import { cn } from '../../lib/utils';
import { useTheme } from '../theme-provider';
import { Button } from '../ui/button';

type SidebarItemProps = {
  to: string;
  icon: React.ElementType;
  label: string;
};

const SidebarItem: React.FC<SidebarItemProps> = ({ to, icon: Icon, label }) => (
  <NavLink
    to={to}
    className={({ isActive }) =>
      cn(
        'flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-all hover:text-primary',
        isActive ? 'bg-muted text-primary' : 'text-muted-foreground',
      )
    }
  >
    <Icon className="h-4 w-4" />
    {label}
  </NavLink>
);

const ThemeToggle: React.FC = () => {
  const { theme, setTheme } = useTheme();

  return (
    <Button
      variant="ghost"
      size="icon"
      onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')}
      title="Toggle Theme"
    >
      <Sun className="h-[1.2rem] w-[1.2rem] rotate-0 scale-100 transition-all dark:-rotate-90 dark:scale-0" />
      <Moon className="absolute h-[1.2rem] w-[1.2rem] rotate-90 scale-0 transition-all dark:rotate-0 dark:scale-100" />
      <span className="sr-only">Toggle theme</span>
    </Button>
  );
};

const MainLayout: React.FC = () => {
  const location = useLocation();
  const pathSegments = location.pathname.split('/').filter(Boolean);

  return (
    <div className="grid min-h-screen w-full md:grid-cols-[220px_1fr] lg:grid-cols-[260px_1fr]">
      <aside className="hidden border-r bg-slate-900 text-slate-50 md:block dark:bg-slate-950">
        <div className="flex h-full max-h-screen flex-col gap-2">
          <div className="flex h-14 items-center border-b border-slate-800 px-4 lg:h-[60px] lg:px-6">
            <NavLink to="/" className="flex items-center gap-2 font-semibold">
              <Music className="h-6 w-6 text-violet-500" />
              <span className="text-lg tracking-tight">Orchestra</span>
            </NavLink>
          </div>
        <nav className="flex-1 px-2 py-4 text-sm font-medium lg:px-4">
          <div className="mb-2 px-4 text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400">
            Menu
          </div>
          <div className="grid gap-1">
            <SidebarItem to="/import" icon={LayoutGrid} label="Dashboard" />
            <SidebarItem to="/processes" icon={FileText} label="Processes" />
            <SidebarItem to="/specs" icon={FileText} label="Specifications" />
            <SidebarItem to="/suites" icon={Box} label="Scenario Suites" />
            <SidebarItem to="/datasets" icon={Database} label="Data Sets" />
            <SidebarItem to="/runs" icon={History} label="Test Runs" />
          </div>
          <div className="mt-6 mb-2 px-4 text-xs font-semibold uppercase tracking-wider text-violet-500 dark:text-violet-400">
            AI Tools
          </div>
          <div className="grid gap-1">
            <SidebarItem to="/scenarios/new" icon={Sparkles} label="Wizard" />
          </div>
          <div className="mt-6 mb-2 px-4 text-xs font-semibold uppercase tracking-wider text-slate-500 dark:text-slate-400">
            Settings
          </div>
          <div className="grid gap-1">
            <SidebarItem to="/settings" icon={Settings} label="Settings" />
          </div>
        </nav>
          <div className="mt-auto border-t border-slate-800 p-4 text-xs text-slate-400">
            Orchestra v0.1.0 (MVP)
          </div>
        </div>
      </aside>

      <div className="flex flex-col bg-background">
        <header className="flex h-14 items-center gap-4 border-b bg-card px-4 lg:h-[60px] lg:px-6">
          <div className="w-full flex-1">
            <nav aria-label="Breadcrumb">
              <ol className="inline-flex items-center space-x-1 md:space-x-3">
                <li className="inline-flex items-center">
                  <NavLink to="/" className="text-sm font-medium text-muted-foreground hover:text-primary">
                    Home
                  </NavLink>
                </li>
                {pathSegments.map((segment, index) => (
                  <li key={segment + index}>
                    <div className="flex items-center">
                      <span className="mx-2 text-muted-foreground">/</span>
                      <span className="text-sm font-medium capitalize text-foreground">{segment}</span>
                    </div>
                  </li>
                ))}
              </ol>
            </nav>
          </div>
          <ThemeToggle />
        </header>

        <main className="flex flex-1 flex-col gap-4 overflow-auto p-4 lg:gap-6 lg:p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default MainLayout;
