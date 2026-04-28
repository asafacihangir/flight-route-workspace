import type { LoginReq } from "@/api/services/authService";
import { getHomeRoute } from "@/routes/role-home";
import { useSignIn } from "@/store/userStore";
import { Alert } from "@/ui/alert";
import { Button } from "@/ui/button";
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from "@/ui/form";
import { Input } from "@/ui/input";
import { cn } from "@/utils";
import type { AxiosError } from "axios";
import { Loader2 } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router";
import { toast } from "sonner";

type LoginErrorKey = "sys.login.invalidCredentials" | "sys.login.invalidRequest" | "sys.login.networkError";

function resolveLoginErrorKey(err: unknown): LoginErrorKey {
	const axiosErr = err as AxiosError | undefined;
	if (axiosErr && !axiosErr.response) {
		return "sys.login.networkError";
	}
	const status = axiosErr?.response?.status;
	if (status === 401) return "sys.login.invalidCredentials";
	if (status === 400) return "sys.login.invalidRequest";
	return "sys.login.invalidRequest";
}

export function LoginForm({ className, ...props }: React.ComponentPropsWithoutRef<"form">) {
	const { t } = useTranslation();
	const [loading, setLoading] = useState(false);
	const [errorKey, setErrorKey] = useState<LoginErrorKey | null>(null);
	const navigate = useNavigate();
	const signIn = useSignIn();

	const form = useForm<LoginReq>({
		defaultValues: { username: "", password: "" },
	});

	const handleFinish = async (values: LoginReq) => {
		setErrorKey(null);
		setLoading(true);
		try {
			const me = await signIn(values);
			toast.success(t("sys.login.loginSuccessTitle"), { closeButton: true });
			navigate(getHomeRoute(me.role), { replace: true });
		} catch (err) {
			setErrorKey(resolveLoginErrorKey(err));
		} finally {
			setLoading(false);
		}
	};

	return (
		<div className={cn("flex flex-col gap-6", className)}>
			<Form {...form} {...props}>
				<form onSubmit={form.handleSubmit(handleFinish)} className="space-y-4">
					<div className="flex flex-col items-center gap-2 text-center">
						<h1 className="text-2xl font-bold">{t("sys.login.signInFormTitle")}</h1>
						<p className="text-balance text-sm text-muted-foreground">{t("sys.login.signInFormDescription")}</p>
					</div>

					{errorKey && <Alert variant="destructive">{t(errorKey)}</Alert>}

					<FormField
						control={form.control}
						name="username"
						rules={{ required: t("sys.login.accountPlaceholder") }}
						render={({ field }) => (
							<FormItem>
								<FormLabel>{t("sys.login.userName")}</FormLabel>
								<FormControl>
									<Input placeholder="systemadmin / systemagency" autoComplete="username" {...field} />
								</FormControl>
								<FormMessage />
							</FormItem>
						)}
					/>

					<FormField
						control={form.control}
						name="password"
						rules={{ required: t("sys.login.passwordPlaceholder") }}
						render={({ field }) => (
							<FormItem>
								<FormLabel>{t("sys.login.password")}</FormLabel>
								<FormControl>
									<Input type="password" autoComplete="current-password" {...field} />
								</FormControl>
								<FormMessage />
							</FormItem>
						)}
					/>

					<Button type="submit" className="w-full" disabled={loading}>
						{loading && <Loader2 className="animate-spin mr-2" />}
						{t("sys.login.loginButton")}
					</Button>
				</form>
			</Form>
		</div>
	);
}

export default LoginForm;
