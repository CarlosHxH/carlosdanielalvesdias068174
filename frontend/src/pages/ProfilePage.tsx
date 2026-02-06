import { useState, useEffect } from 'react';
import { useAuth } from '@/contexts/AuthContext';
import { authService } from '@/services/AuthService';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import {
  Loader2,
  Eye,
  EyeOff,
  User,
  Pencil,
  Shield,
  Hash,
  Calendar,
  LogIn,
  BadgeCheck,
} from 'lucide-react';
import { showApiErrorToast } from '@/lib/errorUtils';
import { toast } from 'sonner';

/**
 * Página de perfil do usuário
 * Exibe dados do usuário e permite editar username/email e alterar senha
 */
export default function ProfilePage() {
  const { user } = useAuth();
  const [editUsername, setEditUsername] = useState('');
  const [editEmail, setEditEmail] = useState('');
  const [senhaAtual, setSenhaAtual] = useState('');
  const [novaSenha, setNovaSenha] = useState('');
  const [confirmarSenha, setConfirmarSenha] = useState('');
  const [showSenhaAtual, setShowSenhaAtual] = useState(false);
  const [showNovaSenha, setShowNovaSenha] = useState(false);
  const [savingProfile, setSavingProfile] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);

  useEffect(() => {
    if (user) {
      setEditUsername(user.username);
      setEditEmail(user.email);
    }
  }, [user]);

  const handleSalvarPerfil = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editUsername.trim() || !editEmail.trim()) {
      toast.error('Preencha todos os campos');
      return;
    }
    if (editUsername.length < 3 || editUsername.length > 50) {
      toast.error('Username deve ter entre 3 e 50 caracteres');
      return;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(editEmail)) {
      toast.error('E-mail inválido');
      return;
    }
    setSavingProfile(true);
    try {
      await authService.atualizarPerfil(editUsername.trim(), editEmail.trim());
      toast.success('Perfil atualizado com sucesso!');
    } catch (error) {
      showApiErrorToast(error, 'Falha ao atualizar perfil');
    } finally {
      setSavingProfile(false);
    }
  };

  const handleAlterarSenha = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!senhaAtual || !novaSenha || !confirmarSenha) {
      toast.error('Preencha todos os campos');
      return;
    }
    if (novaSenha.length < 6) {
      toast.error('A nova senha deve ter pelo menos 6 caracteres');
      return;
    }
    if (novaSenha !== confirmarSenha) {
      toast.error('As senhas não coincidem');
      return;
    }
    setSavingPassword(true);
    try {
      await authService.alterarSenha(senhaAtual, novaSenha);
      toast.success('Senha alterada com sucesso!');
      setSenhaAtual('');
      setNovaSenha('');
      setConfirmarSenha('');
    } catch (error) {
      showApiErrorToast(error, 'Falha ao alterar senha');
    } finally {
      setSavingPassword(false);
    }
  };

  const formatarData = (data?: string) => {
    if (!data) return '-';
    try {
      return new Date(data).toLocaleString('pt-BR');
    } catch {
      return data;
    }
  };

  const formatarRoles = (roles?: string[]) => {
    if (!roles || roles.length === 0) return '-';
    return roles.map((r) => r.replace('ROLE_', '')).join(', ');
  };

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-[280px]">
        <div className="flex flex-col items-center gap-3">
          <Loader2 className="h-8 w-8 animate-spin text-emerald-500" />
          <span className="text-slate-400 text-sm">Carregando perfil...</span>
        </div>
      </div>
    );
  }

  const initial = user.username?.charAt(0)?.toUpperCase() || 'U';

  return (
    <div className="max-w-2xl mx-auto w-full min-w-0">
      {/* Hero header */}
      <header className="relative mb-8 overflow-hidden rounded-2xl bg-gradient-to-br from-slate-800/80 via-slate-800/60 to-slate-900/80 border border-slate-700/80 shadow-xl">
        <div className="absolute inset-0 bg-[radial-gradient(ellipse_80%_50%_at_50%_-20%,rgba(16,185,129,0.15),transparent)]" />
        <div className="relative px-6 py-8 sm:py-10">
          <div className="flex flex-col sm:flex-row items-center sm:items-start gap-6">
            <div className="flex h-24 w-24 shrink-0 items-center justify-center rounded-2xl bg-gradient-to-br from-emerald-500 via-emerald-600 to-teal-600 shadow-lg shadow-emerald-900/30 ring-2 ring-emerald-400/20 text-3xl font-bold text-white">
              {initial}
            </div>
            <div className="text-center sm:text-left flex-1 min-w-0">
              <h1 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
                {user.username}
              </h1>
              <p className="text-slate-400 mt-1 truncate">{user.email}</p>
              <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2 mt-3">
                <span
                  className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium ${
                    user.ativo
                      ? 'bg-emerald-500/20 text-emerald-400 ring-1 ring-emerald-500/30'
                      : 'bg-slate-600/50 text-slate-400 ring-1 ring-slate-500/30'
                  }`}
                >
                  <BadgeCheck className="size-3.5" />
                  {user.ativo ? 'Conta ativa' : 'Inativo'}
                </span>
                {user.roles?.includes('ROLE_ADMIN') && (
                  <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium bg-amber-500/20 text-amber-400 ring-1 ring-amber-500/30">
                    <Shield className="size-3.5" />
                    Administrador
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>
      </header>

      {/* Tabs card */}
      <Card className="w-full border-slate-700/80 bg-slate-800/40 shadow-xl overflow-hidden backdrop-blur-sm">
        <Tabs defaultValue="conta" className="w-full">
          <div className="border-b border-slate-700/80 px-4 sm:px-6 pt-4 pb-1">
            <TabsList className="grid w-full grid-cols-3 h-11 bg-slate-800/80 p-1 gap-1 rounded-lg">
              <TabsTrigger
                value="conta"
                className="inline-flex items-center justify-center gap-2 data-[state=active]:!bg-emerald-600 data-[state=active]:!text-white data-[state=active]:!border-transparent text-slate-400 hover:text-slate-200 rounded-md px-3 py-2 text-sm font-medium transition-colors border border-transparent"
              >
                <User className="size-4 shrink-0" />
                Conta
              </TabsTrigger>
              <TabsTrigger
                value="editar"
                className="inline-flex items-center justify-center gap-2 data-[state=active]:!bg-emerald-600 data-[state=active]:!text-white data-[state=active]:!border-transparent text-slate-400 hover:text-slate-200 rounded-md px-3 py-2 text-sm font-medium transition-colors border border-transparent"
              >
                <Pencil className="size-4 shrink-0" />
                Editar
              </TabsTrigger>
              <TabsTrigger
                value="seguranca"
                className="inline-flex items-center justify-center gap-2 data-[state=active]:!bg-emerald-600 data-[state=active]:!text-white data-[state=active]:!border-transparent text-slate-400 hover:text-slate-200 rounded-md px-3 py-2 text-sm font-medium transition-colors border border-transparent"
              >
                <Shield className="size-4 shrink-0" />
                Segurança
              </TabsTrigger>
            </TabsList>
          </div>

          <TabsContent value="conta" className="mt-0">
            <CardContent className="p-6 sm:p-8">
              <div className="mb-6">
                <CardTitle className="text-lg font-semibold text-white">Informações da conta</CardTitle>
                <CardDescription className="text-slate-400 mt-1">
                  Dados da sua conta (somente leitura)
                </CardDescription>
              </div>
              <div className="grid gap-4 sm:grid-cols-2">
                <InfoField
                  icon={<Hash className="size-4 text-emerald-400/80" />}
                  label="ID"
                  value={String(user.id)}
                />
                <InfoField
                  icon={<BadgeCheck className="size-4 text-emerald-400/80" />}
                  label="Papéis"
                  value={formatarRoles(user.roles)}
                />
                <InfoField
                  icon={<Calendar className="size-4 text-emerald-400/80" />}
                  label="Data de criação"
                  value={formatarData(user.createdAt)}
                  className="sm:col-span-2"
                />
                <InfoField
                  icon={<LogIn className="size-4 text-emerald-400/80" />}
                  label="Último login"
                  value={formatarData(user.lastLogin)}
                  className="sm:col-span-2"
                />
              </div>
            </CardContent>
          </TabsContent>

          <TabsContent value="editar" className="mt-0">
            <CardContent className="p-6 sm:p-8">
              <div className="mb-6">
                <CardTitle className="text-lg font-semibold text-white">Editar perfil</CardTitle>
                <CardDescription className="text-slate-400 mt-1">
                  Atualize seu username e e-mail
                </CardDescription>
              </div>
              <form onSubmit={handleSalvarPerfil} className="space-y-5 max-w-md mx-auto w-full">
                <div className="space-y-2">
                  <Label htmlFor="profile-username" className="text-slate-300 font-medium">
                    Username
                  </Label>
                  <Input
                    id="profile-username"
                    type="text"
                    placeholder="seu_usuario"
                    value={editUsername}
                    onChange={(e) => setEditUsername(e.target.value)}
                    disabled={savingProfile}
                    className="h-11 bg-slate-700/80 border-slate-600 text-white placeholder:text-slate-500 focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 rounded-lg"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="profile-email" className="text-slate-300 font-medium">
                    E-mail
                  </Label>
                  <Input
                    id="profile-email"
                    type="email"
                    placeholder="seu@email.com"
                    value={editEmail}
                    onChange={(e) => setEditEmail(e.target.value)}
                    disabled={savingProfile}
                    className="h-11 bg-slate-700/80 border-slate-600 text-white placeholder:text-slate-500 focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 rounded-lg"
                  />
                </div>
                <Button
                  type="submit"
                  className="h-11 px-6 bg-emerald-600 hover:bg-emerald-500 text-white shadow-lg shadow-emerald-900/30 rounded-lg"
                  disabled={savingProfile}
                >
                  {savingProfile ? (
                    <Loader2 className="mr-2 h-4 w-4 animate-spin shrink-0" />
                  ) : null}
                  Salvar alterações
                </Button>
              </form>
            </CardContent>
          </TabsContent>

          <TabsContent value="seguranca" className="mt-0">
            <CardContent className="p-6 sm:p-8">
              <div className="mb-6">
                <CardTitle className="text-lg font-semibold text-white">Alterar senha</CardTitle>
                <CardDescription className="text-slate-400 mt-1">
                  Informe a senha atual e a nova senha para atualizar sua credencial
                </CardDescription>
              </div>
              <form onSubmit={handleAlterarSenha} className="space-y-5 max-w-md mx-auto w-full">
                <div className="space-y-2">
                  <Label htmlFor="profile-senha-atual" className="text-slate-300 font-medium">
                    Senha atual
                  </Label>
                  <div className="relative">
                    <Input
                      id="profile-senha-atual"
                      type={showSenhaAtual ? 'text' : 'password'}
                      placeholder="••••••••"
                      value={senhaAtual}
                      onChange={(e) => setSenhaAtual(e.target.value)}
                      disabled={savingPassword}
                      className="h-11 bg-slate-700/80 border-slate-600 text-white placeholder:text-slate-500 pr-12 focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 rounded-lg"
                    />
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      className="absolute right-1 top-1/2 -translate-y-1/2 h-9 w-9 text-slate-400 hover:text-white hover:bg-slate-600/50 rounded-md"
                      onClick={() => setShowSenhaAtual(!showSenhaAtual)}
                      aria-label={showSenhaAtual ? 'Ocultar senha' : 'Mostrar senha'}
                    >
                      {showSenhaAtual ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </Button>
                  </div>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="profile-nova-senha" className="text-slate-300 font-medium">
                    Nova senha
                  </Label>
                  <div className="relative">
                    <Input
                      id="profile-nova-senha"
                      type={showNovaSenha ? 'text' : 'password'}
                      placeholder="••••••••"
                      value={novaSenha}
                      onChange={(e) => setNovaSenha(e.target.value)}
                      disabled={savingPassword}
                      className="h-11 bg-slate-700/80 border-slate-600 text-white placeholder:text-slate-500 pr-12 focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 rounded-lg"
                    />
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon"
                      className="absolute right-1 top-1/2 -translate-y-1/2 h-9 w-9 text-slate-400 hover:text-white hover:bg-slate-600/50 rounded-md"
                      onClick={() => setShowNovaSenha(!showNovaSenha)}
                      aria-label={showNovaSenha ? 'Ocultar senha' : 'Mostrar senha'}
                    >
                      {showNovaSenha ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </Button>
                  </div>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="profile-confirmar-senha" className="text-slate-300 font-medium">
                    Confirmar nova senha
                  </Label>
                  <Input
                    id="profile-confirmar-senha"
                    type="password"
                    placeholder="••••••••"
                    value={confirmarSenha}
                    onChange={(e) => setConfirmarSenha(e.target.value)}
                    disabled={savingPassword}
                    className="h-11 bg-slate-700/80 border-slate-600 text-white placeholder:text-slate-500 focus:ring-2 focus:ring-emerald-500/50 focus:border-emerald-500/50 rounded-lg"
                  />
                </div>
                <Button
                  type="submit"
                  className="h-11 px-6 bg-slate-700 hover:bg-slate-600 text-white border border-slate-600 rounded-lg"
                  disabled={savingPassword}
                >
                  {savingPassword ? (
                    <Loader2 className="mr-2 h-4 w-4 animate-spin shrink-0" />
                  ) : null}
                  Alterar senha
                </Button>
              </form>
            </CardContent>
          </TabsContent>
        </Tabs>
      </Card>
    </div>
  );
}

function InfoField({
  icon,
  label,
  value,
  className = '',
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
  className?: string;
}) {
  return (
    <div
      className={`flex items-start gap-3 p-4 rounded-xl bg-slate-800/60 border border-slate-700/60 ${className}`}
    >
      <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-slate-700/80">
        {icon}
      </div>
      <div className="min-w-0 flex-1">
        <p className="text-xs font-medium text-slate-500 uppercase tracking-wider">{label}</p>
        <p className="text-white font-medium mt-0.5 truncate">{value}</p>
      </div>
    </div>
  );
}
